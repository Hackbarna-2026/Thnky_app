package com.thnky.challenge;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thnky.ai.ChatMessage;
import com.thnky.ai.GeneratedChallengeContent;
import com.thnky.ai.GeneratedChallengeSchema;
import com.thnky.ai.NebiusClient;
import com.thnky.ai.OddOneOutContent;
import com.thnky.ai.OddOneOutSchema;
import com.thnky.ai.ShapeSequenceContent;
import com.thnky.ai.ShapeSequenceSchema;
import com.thnky.domain.Challenge;
import com.thnky.domain.ChallengeType;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Lang;
import com.thnky.domain.Skill;
import com.thnky.profile.AttemptResult;
import com.thnky.profile.LearnerProfileRepository;

/**
 * Generates challenges with Nebius instead of picking from the static bank.
 * Each skill has one or more challenge types it can be generated as; a type
 * is picked at random per request for variety. When a learner's recent
 * results are known, they inform both the difficulty (if none was requested
 * explicitly) and a note in the prompt so topic and tone adapt too
 * (CLAUDE.md section 10).
 */
@Component
public class NebiusChallengeSource implements ChallengeSource, ChallengeLookup {

    private static final Logger log = LoggerFactory.getLogger(NebiusChallengeSource.class);
    private static final String SYSTEM_PROMPT_RESOURCE = "/prompts/generator-system.md";
    private static final String SCHEMA_NAME = "generated_challenge";
    private static final int FEW_SHOT_COUNT = 2;
    // A generator benefits from variety; unlike the judge, it is not making a
    // pass/fail call, so the default sampling temperature is fine here.
    private static final double GENERATOR_TEMPERATURE = 1.0;
    private static final double STRONG_ACCURACY = 0.8;
    private static final double WEAK_ACCURACY = 0.4;
    private static final double LOW_HINTS_AVG = 1.0;
    private static final double HIGH_HINTS_AVG = 2.5;

    private static final String SHAPE_SEQUENCE_SYSTEM_PROMPT_RESOURCE = "/prompts/shape-sequence-system.md";
    private static final String SHAPE_SEQUENCE_SCHEMA_NAME = "shape_sequence";
    private static final String ODD_ONE_OUT_SYSTEM_PROMPT_RESOURCE = "/prompts/odd-one-out-system.md";
    private static final String ODD_ONE_OUT_SCHEMA_NAME = "odd_one_out";
    private static final int ODD_ONE_OUT_TILE_COUNT = 6;
    // How a logic/choice request splits between a text puzzle and the two
    // visual ones, for variety. Must sum to <= 1; the remainder is text.
    private static final double SHAPE_SEQUENCE_PROBABILITY = 0.25;
    private static final double ODD_ONE_OUT_PROBABILITY = 0.25;

    private static final Map<Skill, List<ChallengeType>> GENERATABLE_TYPES = Map.of(
            Skill.LOGIC, List.of(ChallengeType.CHOICE),
            Skill.CODE, List.of(ChallengeType.LINES, ChallengeType.CODE),
            Skill.CRITICAL, List.of(ChallengeType.TEXT)
    );

    private final NebiusClient nebiusClient;
    private final StaticChallengeSource staticChallengeSource;
    private final LearnerProfileRepository profileRepository;
    private final ChallengeCache cache;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    private final String shapeSequenceSystemPrompt;
    private final String oddOneOutSystemPrompt;
    private final Map<String, Challenge> generated = new ConcurrentHashMap<>();

    public NebiusChallengeSource(
            NebiusClient nebiusClient,
            StaticChallengeSource staticChallengeSource,
            LearnerProfileRepository profileRepository,
            ChallengeCache cache,
            ObjectMapper objectMapper
    ) {
        this.nebiusClient = nebiusClient;
        this.staticChallengeSource = staticChallengeSource;
        this.profileRepository = profileRepository;
        this.cache = cache;
        this.objectMapper = objectMapper;
        this.systemPrompt = readResource(SYSTEM_PROMPT_RESOURCE);
        this.shapeSequenceSystemPrompt = readResource(SHAPE_SEQUENCE_SYSTEM_PROMPT_RESOURCE);
        this.oddOneOutSystemPrompt = readResource(ODD_ONE_OUT_SYSTEM_PROMPT_RESOURCE);
    }

    @Override
    public Challenge next(Skill skill, Difficulty diff, Lang lang, String userId) {
        // Only cache when a difficulty was requested explicitly. Caching the
        // "no diff given" path would pin the personalized suggestion from the
        // first call, even after the learner's recent results change it.
        boolean cacheable = diff != null;
        if (cacheable) {
            Optional<Challenge> cached = cache.get(skill, diff, lang, userId)
                    .filter(c -> !alreadyAttempted(userId, skill, c.id()));
            if (cached.isPresent()) {
                return cached.get();
            }
        }

        List<ChallengeType> options = GENERATABLE_TYPES.get(skill);
        if (options == null || options.isEmpty()) {
            throw new ChallengeGenerationException("Nebius does not generate " + skill + " challenges yet");
        }
        ChallengeType type = options.get(ThreadLocalRandom.current().nextInt(options.size()));
        List<AttemptResult> history = (userId == null || userId.isBlank())
                ? List.of()
                : profileRepository.recentResults(userId, skill);
        Difficulty effectiveDiff = diff != null ? diff : suggestDifficulty(history);
        Lang effectiveLang = resolveLang(skill, lang);
        String profileNote = buildProfileNote(history);

        Challenge challenge = (skill == Skill.LOGIC && type == ChallengeType.CHOICE)
                ? generateLogicChoice(effectiveDiff, profileNote)
                : generateTextual(skill, effectiveDiff, type, effectiveLang, profileNote);

        generated.put(challenge.id(), challenge);
        if (cacheable) {
            cache.put(skill, diff, lang, userId, challenge);
        }
        return challenge;
    }

    private Challenge generateTextual(Skill skill, Difficulty diff, ChallengeType type, Lang lang, String profileNote) {
        List<ChatMessage> messages = buildMessages(skill, diff, type, lang, profileNote);
        JsonNode schema = GeneratedChallengeSchema.forType(type);
        String rawJson = nebiusClient.complete(messages, SCHEMA_NAME, schema, GENERATOR_TEMPERATURE);
        GeneratedChallengeContent content = parseContent(rawJson);

        Challenge challenge = assemble(skill, lang, diff, type, content);
        try {
            validate(challenge);
        } catch (ChallengeGenerationException e) {
            log.warn("Rejected Nebius generation for skill={}, type={}: {} — raw content: {}",
                    skill, type, e.getMessage(), rawJson);
            throw e;
        }
        return challenge;
    }

    /** Splits a logic/choice request between text and the two visual puzzle families. */
    private Challenge generateLogicChoice(Difficulty diff, String profileNote) {
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < SHAPE_SEQUENCE_PROBABILITY) {
            return generateShapeSequence(diff, profileNote);
        }
        if (roll < SHAPE_SEQUENCE_PROBABILITY + ODD_ONE_OUT_PROBABILITY) {
            return generateOddOneOut(diff, profileNote);
        }
        return generateTextual(Skill.LOGIC, diff, ChallengeType.CHOICE, null, profileNote);
    }

    /**
     * A visual logic puzzle: Nebius picks the pattern parameters and writes
     * the copy, {@link RotationSequenceSvg} draws every pixel — no SVG ever
     * comes from the model (CLAUDE.md section 11).
     */
    private Challenge generateShapeSequence(Difficulty diff, String profileNote) {
        String request = "Generate one " + diff.name().toLowerCase() + " shape-sequence challenge.";
        List<ChatMessage> messages = List.of(
                ChatMessage.system(shapeSequenceSystemPrompt),
                ChatMessage.user(profileNote == null ? request : profileNote + "\n\n" + request)
        );
        String rawJson = nebiusClient.complete(messages, SHAPE_SEQUENCE_SCHEMA_NAME, ShapeSequenceSchema.build(), GENERATOR_TEMPERATURE);

        ShapeSequenceContent content;
        try {
            content = objectMapper.readValue(rawJson, ShapeSequenceContent.class);
        } catch (IOException e) {
            throw new ChallengeGenerationException("Shape-sequence response did not match the schema: " + e.getMessage());
        }
        try {
            validateShapeSequence(content);
        } catch (ChallengeGenerationException e) {
            log.warn("Rejected Nebius shape-sequence generation: {} — raw content: {}", e.getMessage(), rawJson);
            throw e;
        }

        String figure = RotationSequenceSvg.figure(content.rotationStep(), content.startSolid(), content.visibleSteps());
        RotationSequenceSvg.Options tiles = RotationSequenceSvg.options(
                content.rotationStep(), content.startSolid(), content.visibleSteps(),
                ThreadLocalRandom.current().nextLong());

        return new Challenge(
                "gen-" + UUID.randomUUID(),
                Skill.LOGIC, null, diff, ChallengeType.CHOICE,
                content.hook(), content.title(), content.desc(),
                List.of("A", "B", "C", "D"), tiles.correctIndex(), tiles.svgs(), figure,
                null, null, null,
                content.hints(), content.good(), content.improve(), content.insight()
        );
    }

    private void validateShapeSequence(ShapeSequenceContent c) {
        requireNonBlank(c.hook(), "hook");
        requireNonBlank(c.title(), "title");
        requireNonBlank(c.desc(), "desc");
        requireNonBlank(c.good(), "good");
        requireNonBlank(c.improve(), "improve");
        requireNonBlank(c.insight(), "insight");
        if (c.hints() == null || c.hints().size() != 3) {
            throw new ChallengeGenerationException("Shape-sequence challenge must have exactly 3 hints");
        }
        if (c.rotationStep() != 60 && c.rotationStep() != 90) {
            throw new ChallengeGenerationException("Shape-sequence rotationStep must be 60 or 90, was " + c.rotationStep());
        }
        if (c.visibleSteps() != 2 && c.visibleSteps() != 3) {
            throw new ChallengeGenerationException("Shape-sequence visibleSteps must be 2 or 3, was " + c.visibleSteps());
        }
    }

    /**
     * A second visual logic puzzle: Nebius picks the polygon side counts and
     * which tile breaks the "dots equal sides" rule, {@link PolygonTileSvg}
     * draws every tile — again, no SVG from the model.
     */
    private Challenge generateOddOneOut(Difficulty diff, String profileNote) {
        String request = "Generate one " + diff.name().toLowerCase() + " odd-one-out challenge.";
        List<ChatMessage> messages = List.of(
                ChatMessage.system(oddOneOutSystemPrompt),
                ChatMessage.user(profileNote == null ? request : profileNote + "\n\n" + request)
        );
        String rawJson = nebiusClient.complete(messages, ODD_ONE_OUT_SCHEMA_NAME, OddOneOutSchema.build(), GENERATOR_TEMPERATURE);

        OddOneOutContent content;
        try {
            content = objectMapper.readValue(rawJson, OddOneOutContent.class);
        } catch (IOException e) {
            throw new ChallengeGenerationException("Odd-one-out response did not match the schema: " + e.getMessage());
        }
        try {
            validateOddOneOut(content);
        } catch (ChallengeGenerationException e) {
            log.warn("Rejected Nebius odd-one-out generation: {} — raw content: {}", e.getMessage(), rawJson);
            throw e;
        }

        PolygonTileSvg.Puzzle puzzle = PolygonTileSvg.build(content.sides(), content.oddIndex(), content.violationDelta());
        List<String> letters = List.of("A", "B", "C", "D", "E", "F");

        return new Challenge(
                "gen-" + UUID.randomUUID(),
                Skill.LOGIC, null, diff, ChallengeType.CHOICE,
                content.hook(), content.title(), content.desc(),
                letters, puzzle.oddIndex(), puzzle.tiles(), null,
                null, null, null,
                content.hints(), content.good(), content.improve(), content.insight()
        );
    }

    private void validateOddOneOut(OddOneOutContent c) {
        requireNonBlank(c.hook(), "hook");
        requireNonBlank(c.title(), "title");
        requireNonBlank(c.desc(), "desc");
        requireNonBlank(c.good(), "good");
        requireNonBlank(c.improve(), "improve");
        requireNonBlank(c.insight(), "insight");
        if (c.hints() == null || c.hints().size() != 3) {
            throw new ChallengeGenerationException("Odd-one-out challenge must have exactly 3 hints");
        }
        if (c.sides() == null || c.sides().size() != ODD_ONE_OUT_TILE_COUNT) {
            throw new ChallengeGenerationException("Odd-one-out challenge needs exactly " + ODD_ONE_OUT_TILE_COUNT + " tiles");
        }
        for (Integer sideCount : c.sides()) {
            if (sideCount == null || sideCount < 3 || sideCount > 6) {
                throw new ChallengeGenerationException("Odd-one-out side count out of range: " + sideCount);
            }
        }
        if (c.oddIndex() < 0 || c.oddIndex() >= ODD_ONE_OUT_TILE_COUNT) {
            throw new ChallengeGenerationException("Odd-one-out oddIndex out of range: " + c.oddIndex());
        }
        if (c.violationDelta() == 0) {
            throw new ChallengeGenerationException("Odd-one-out violationDelta must not be 0");
        }
    }

    @Override
    public Optional<Challenge> findById(String id) {
        Challenge inMemory = generated.get(id);
        return inMemory != null ? Optional.of(inMemory) : cache.findById(id);
    }

    /**
     * A cache hit whose challenge this learner already answered is not a
     * "next challenge" — it is the same one again. Since the real frontend
     * always sends an explicit diff, skipping this check would mean the
     * cache serves one generated challenge per (skill, diff, lang) forever.
     */
    private boolean alreadyAttempted(String userId, Skill skill, String challengeId) {
        return userId != null && !userId.isBlank() && profileRepository.hasAttempted(userId, skill, challengeId);
    }

    private Lang resolveLang(Skill skill, Lang requested) {
        if (skill != Skill.CODE) {
            return null;
        }
        if (requested != null) {
            return requested;
        }
        Lang[] langs = Lang.values();
        return langs[ThreadLocalRandom.current().nextInt(langs.length)];
    }

    /**
     * With no explicit difficulty and no history, start in the middle.
     * Otherwise nudge from recent accuracy and hint usage — simple on
     * purpose, so it stays easy to explain.
     */
    private Difficulty suggestDifficulty(List<AttemptResult> history) {
        if (history.isEmpty()) {
            return Difficulty.MEDIUM;
        }
        double accuracy = history.stream().mapToDouble(r -> r.correct() ? 1 : 0).average().orElse(0);
        double avgHints = history.stream().mapToInt(AttemptResult::hintsUsed).average().orElse(0);
        if (accuracy >= STRONG_ACCURACY && avgHints <= LOW_HINTS_AVG) {
            return Difficulty.HARD;
        }
        if (accuracy <= WEAK_ACCURACY || avgHints >= HIGH_HINTS_AVG) {
            return Difficulty.EASY;
        }
        return Difficulty.MEDIUM;
    }

    /** A short line for the prompt so topic and tone adapt too, not just the difficulty label. */
    private String buildProfileNote(List<AttemptResult> history) {
        if (history.isEmpty()) {
            return null;
        }
        long correctCount = history.stream().filter(AttemptResult::correct).count();
        double avgHints = history.stream().mapToInt(AttemptResult::hintsUsed).average().orElse(0);
        return "The learner's last %d attempts at this skill: %d correct, averaging %.1f hints used. "
                .formatted(history.size(), correctCount, avgHints)
                + (correctCount >= history.size() - 1
                        ? "They are doing well — pick a less obvious angle than usual."
                        : "Keep the core idea simple and the phrasing extra clear.");
    }

    private List<ChatMessage> buildMessages(Skill skill, Difficulty diff, ChallengeType type, Lang lang, String profileNote) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));
        for (Challenge example : pickFewShotExamples(skill, type, lang)) {
            messages.add(ChatMessage.user(
                    describeRequest(example.skill(), example.diff(), example.type(), example.lang())));
            messages.add(ChatMessage.assistant(toContentJson(example)));
        }
        String finalRequest = describeRequest(skill, diff, type, lang);
        messages.add(ChatMessage.user(profileNote == null ? finalRequest : profileNote + "\n\n" + finalRequest));
        return messages;
    }

    private List<Challenge> pickFewShotExamples(Skill skill, ChallengeType type, Lang lang) {
        return staticChallengeSource.all().stream()
                .filter(c -> c.skill() == skill && c.type() == type)
                .filter(c -> c.figure() == null && c.optionsSvg() == null)
                .sorted(Comparator.comparingInt(c -> lang != null && c.lang() == lang ? 0 : 1))
                .limit(FEW_SHOT_COUNT)
                .toList();
    }

    /**
     * "code" is both a skill and a challenge type — asking for "one code code
     * challenge" measurably confused the model into generating a choice
     * shape instead (seen in testing: options/answer filled in, starter
     * left null). Describing the type in plain words for every type, not
     * just where skill and type happen to collide, sidesteps that.
     */
    private String describeRequest(Skill skill, Difficulty diff, ChallengeType type, Lang lang) {
        String typeDescription = switch (type) {
            case CHOICE -> "multiple-choice";
            case LINES -> "find-the-bug";
            case CODE -> "write-a-function";
            case TEXT -> "free-text";
        };
        StringBuilder request = new StringBuilder("Generate one ")
                .append(diff.name().toLowerCase())
                .append(' ').append(typeDescription)
                .append(" challenge for the ").append(skill.name().toLowerCase()).append(" skill");
        if (lang != null) {
            request.append(", in ").append(lang.displayName());
        }
        return request.append('.').toString();
    }

    private String toContentJson(Challenge c) {
        GeneratedChallengeContent content = new GeneratedChallengeContent(
                c.hook(), c.title(), c.desc(), c.options(), c.answer(),
                c.starter(), c.lines(), c.file(), c.hints(), c.good(), c.improve(), c.insight()
        );
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize few-shot example " + c.id(), e);
        }
    }

    private GeneratedChallengeContent parseContent(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, GeneratedChallengeContent.class);
        } catch (IOException e) {
            throw new ChallengeGenerationException("Nebius response did not match the schema: " + e.getMessage());
        }
    }

    /**
     * The model is told which fields apply to its type, but it does not always
     * comply (seen in testing: a "lines" challenge with "options" filled in
     * anyway). Null out whatever does not belong to {@code type} regardless of
     * what came back, so the contract holds even when the prompt is ignored.
     */
    private Challenge assemble(Skill skill, Lang lang, Difficulty diff, ChallengeType type, GeneratedChallengeContent c) {
        boolean isChoice = type == ChallengeType.CHOICE;
        boolean isLines = type == ChallengeType.LINES;
        boolean isCode = type == ChallengeType.CODE;
        boolean hasAnswerIndex = isChoice || isLines;
        return new Challenge(
                "gen-" + UUID.randomUUID(),
                skill, lang, diff, type,
                c.hook(), c.title(), c.desc(),
                isChoice ? c.options() : null,
                hasAnswerIndex ? c.answer() : null,
                null, null,
                isCode ? c.starter() : null,
                isLines ? c.lines() : null,
                isLines ? c.file() : null,
                c.hints(), c.good(), c.improve(), c.insight()
        );
    }

    private void validate(Challenge c) {
        requireNonBlank(c.hook(), "hook");
        requireNonBlank(c.title(), "title");
        requireNonBlank(c.desc(), "desc");
        requireNonBlank(c.good(), "good");
        requireNonBlank(c.improve(), "improve");
        requireNonBlank(c.insight(), "insight");
        if (c.hints() == null || c.hints().size() != 3) {
            throw new ChallengeGenerationException("Generated challenge must have exactly 3 hints");
        }

        switch (c.type()) {
            case CHOICE -> {
                if (c.options() == null || c.options().size() < 2) {
                    throw new ChallengeGenerationException("Generated choice challenge needs at least 2 options");
                }
                if (c.answer() == null || c.answer() < 0 || c.answer() >= c.options().size()) {
                    throw new ChallengeGenerationException("Generated choice challenge has an out-of-range answer");
                }
            }
            case LINES -> {
                if (c.lines() == null || c.lines().isEmpty()) {
                    throw new ChallengeGenerationException("Generated lines challenge has no lines");
                }
                if (c.file() == null || c.file().isBlank()) {
                    throw new ChallengeGenerationException("Generated lines challenge has no file name");
                }
                if (c.answer() == null || c.answer() < 0 || c.answer() >= c.lines().size()) {
                    throw new ChallengeGenerationException("Generated lines challenge has an out-of-range answer");
                }
            }
            case CODE -> {
                if (c.starter() == null || c.starter().isBlank()) {
                    throw new ChallengeGenerationException("Generated code challenge has no starter");
                }
            }
            case TEXT -> {
                // desc, hints, good, improve, insight are already required above; nothing type-specific.
            }
        }
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ChallengeGenerationException("Generated challenge is missing " + field);
        }
    }

    private static String readResource(String path) {
        try (InputStream in = NebiusChallengeSource.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }

}
