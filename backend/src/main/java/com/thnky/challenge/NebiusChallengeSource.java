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

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thnky.ai.ChatMessage;
import com.thnky.ai.GeneratedChallengeContent;
import com.thnky.ai.NebiusClient;
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

    private static final String SYSTEM_PROMPT_RESOURCE = "/prompts/generator-system.md";
    private static final String SCHEMA_RESOURCE = "/prompts/generated-challenge.schema.json";
    private static final String SCHEMA_NAME = "generated_challenge";
    private static final int FEW_SHOT_COUNT = 2;
    private static final double STRONG_ACCURACY = 0.8;
    private static final double WEAK_ACCURACY = 0.4;
    private static final double LOW_HINTS_AVG = 1.0;
    private static final double HIGH_HINTS_AVG = 2.5;

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
    private final JsonNode schema;
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
        this.schema = readSchema(objectMapper);
    }

    @Override
    public Challenge next(Skill skill, Difficulty diff, Lang lang, String userId) {
        // Only cache when a difficulty was requested explicitly. Caching the
        // "no diff given" path would pin the personalized suggestion from the
        // first call, even after the learner's recent results change it.
        boolean cacheable = diff != null;
        if (cacheable) {
            Optional<Challenge> cached = cache.get(skill, diff, lang, userId);
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

        List<ChatMessage> messages = buildMessages(skill, effectiveDiff, type, effectiveLang, buildProfileNote(history));
        String rawJson = nebiusClient.complete(messages, SCHEMA_NAME, schema);
        GeneratedChallengeContent content = parseContent(rawJson);

        Challenge challenge = assemble(skill, effectiveLang, effectiveDiff, type, content);
        validate(challenge);

        generated.put(challenge.id(), challenge);
        if (cacheable) {
            cache.put(skill, diff, lang, userId, challenge);
        }
        return challenge;
    }

    @Override
    public Optional<Challenge> findById(String id) {
        Challenge inMemory = generated.get(id);
        return inMemory != null ? Optional.of(inMemory) : cache.findById(id);
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

    private String describeRequest(Skill skill, Difficulty diff, ChallengeType type, Lang lang) {
        StringBuilder request = new StringBuilder("Generate one ")
                .append(diff.name().toLowerCase())
                .append(' ').append(type.name().toLowerCase())
                .append(' ').append(skill.name().toLowerCase())
                .append(" challenge");
        if (lang != null) {
            request.append(" in ").append(lang.displayName());
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

    private static JsonNode readSchema(ObjectMapper objectMapper) {
        try (InputStream in = NebiusChallengeSource.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + SCHEMA_RESOURCE);
            }
            return objectMapper.readTree(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + SCHEMA_RESOURCE, e);
        }
    }
}
