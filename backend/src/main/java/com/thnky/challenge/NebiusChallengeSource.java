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

/**
 * Generates challenges with Nebius instead of picking from the static bank.
 * Only produces types gradeable locally today (choice, lines) — text and code
 * generation wait for a model-backed grader (CLAUDE.md section 13, step 4),
 * since {@code HeuristicAnswerGrader} has no criteria for anything it did not
 * ship with.
 */
@Component
public class NebiusChallengeSource implements ChallengeSource, ChallengeLookup {

    private static final String SYSTEM_PROMPT_RESOURCE = "/prompts/generator-system.md";
    private static final String SCHEMA_RESOURCE = "/prompts/generated-challenge.schema.json";
    private static final String SCHEMA_NAME = "generated_challenge";
    private static final int FEW_SHOT_COUNT = 2;

    private static final Map<Skill, ChallengeType> GENERATABLE_TYPES = Map.of(
            Skill.LOGIC, ChallengeType.CHOICE,
            Skill.CODE, ChallengeType.LINES
    );

    private final NebiusClient nebiusClient;
    private final StaticChallengeSource staticChallengeSource;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    private final JsonNode schema;
    private final Map<String, Challenge> generated = new ConcurrentHashMap<>();

    public NebiusChallengeSource(
            NebiusClient nebiusClient,
            StaticChallengeSource staticChallengeSource,
            ObjectMapper objectMapper
    ) {
        this.nebiusClient = nebiusClient;
        this.staticChallengeSource = staticChallengeSource;
        this.objectMapper = objectMapper;
        this.systemPrompt = readResource(SYSTEM_PROMPT_RESOURCE);
        this.schema = readSchema(objectMapper);
    }

    @Override
    public Challenge next(Skill skill, Difficulty diff, Lang lang) {
        ChallengeType type = GENERATABLE_TYPES.get(skill);
        if (type == null) {
            throw new ChallengeGenerationException("Nebius does not generate " + skill + " challenges yet");
        }
        Difficulty effectiveDiff = diff != null ? diff : randomDifficulty();
        Lang effectiveLang = resolveLang(skill, lang);

        List<ChatMessage> messages = buildMessages(skill, effectiveDiff, type, effectiveLang);
        String rawJson = nebiusClient.complete(messages, SCHEMA_NAME, schema);
        GeneratedChallengeContent content = parseContent(rawJson);

        Challenge challenge = assemble(skill, effectiveLang, effectiveDiff, type, content);
        validate(challenge);

        generated.put(challenge.id(), challenge);
        return challenge;
    }

    @Override
    public Optional<Challenge> findById(String id) {
        return Optional.ofNullable(generated.get(id));
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

    private Difficulty randomDifficulty() {
        Difficulty[] values = Difficulty.values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    private List<ChatMessage> buildMessages(Skill skill, Difficulty diff, ChallengeType type, Lang lang) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(systemPrompt));
        for (Challenge example : pickFewShotExamples(skill, type, lang)) {
            messages.add(ChatMessage.user(
                    describeRequest(example.skill(), example.diff(), example.type(), example.lang())));
            messages.add(ChatMessage.assistant(toContentJson(example)));
        }
        messages.add(ChatMessage.user(describeRequest(skill, diff, type, lang)));
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
        return new Challenge(
                "gen-" + UUID.randomUUID(),
                skill, lang, diff, type,
                c.hook(), c.title(), c.desc(),
                isChoice ? c.options() : null,
                c.answer(),
                null, null,
                null,
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
            default -> throw new ChallengeGenerationException("Unsupported generated challenge type: " + c.type());
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
