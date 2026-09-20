package com.thnky.grading;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thnky.ai.ChatMessage;
import com.thnky.ai.JudgeVerdict;
import com.thnky.ai.NebiusClient;
import com.thnky.ai.NebiusRequestException;
import com.thnky.domain.Answer;
import com.thnky.domain.Challenge;
import com.thnky.domain.ChallengeType;

/**
 * The Nebius judge: for text and code, correctness is a matter of judgment,
 * not index comparison. One call returns the verdict and all three feedback
 * fields together, personalized to what the learner actually wrote — it
 * never repeats the solution (CLAUDE.md section 7).
 */
@Component
public class ModelGrader implements AnswerGrader {

    private static final String SYSTEM_PROMPT_RESOURCE = "/prompts/judge-system.md";
    private static final String SCHEMA_RESOURCE = "/prompts/judge-verdict.schema.json";
    private static final String SCHEMA_NAME = "judge_verdict";
    // Low, not zero: a judge should grade the same answer the same way every
    // time, not vary with sampling. Zero can make some models degenerate
    // (repeat a phrase, get stuck), so this stays just above it.
    private static final double TEMPERATURE = 0.1;

    private final NebiusClient nebiusClient;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    private final JsonNode schema;

    public ModelGrader(NebiusClient nebiusClient, ObjectMapper objectMapper) {
        this.nebiusClient = nebiusClient;
        this.objectMapper = objectMapper;
        this.systemPrompt = readResource(SYSTEM_PROMPT_RESOURCE);
        this.schema = readSchema(objectMapper);
    }

    @Override
    public boolean supports(ChallengeType type) {
        return type == ChallengeType.TEXT || type == ChallengeType.CODE;
    }

    @Override
    public GradeResult grade(Challenge challenge, Answer answer, int hintsUsed, int secondsSpent) {
        if (!(answer instanceof Answer.Text text)) {
            throw new IllegalArgumentException("ModelGrader only grades free-text answers");
        }

        List<ChatMessage> messages = List.of(
                ChatMessage.system(systemPrompt),
                ChatMessage.user(buildUserPrompt(challenge, text.value(), hintsUsed, secondsSpent))
        );
        String rawJson = nebiusClient.complete(messages, SCHEMA_NAME, schema, TEMPERATURE);
        JudgeVerdict verdict = parse(rawJson);
        validate(verdict);

        return new GradeResult(verdict.correct(), verdict.good(), verdict.improve(), verdict.insight());
    }

    private String buildUserPrompt(Challenge challenge, String answer, int hintsUsed, int secondsSpent) {
        StringBuilder prompt = new StringBuilder()
                .append("Challenge (").append(challenge.type().name().toLowerCase())
                .append(", ").append(challenge.diff().name().toLowerCase()).append("):\n")
                .append(challenge.desc()).append("\n\n");
        if (challenge.starter() != null) {
            prompt.append("Starter code:\n").append(challenge.starter()).append("\n\n");
        }
        prompt.append("Hints used: ").append(hintsUsed).append(" of 3\n")
                .append("Time spent: ").append(secondsSpent).append(" seconds\n\n")
                .append("Learner's answer:\n").append(answer);
        return prompt.toString();
    }

    private JudgeVerdict parse(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, JudgeVerdict.class);
        } catch (IOException e) {
            throw new NebiusRequestException("Judge response did not match the schema: " + e.getMessage(), e);
        }
    }

    private void validate(JudgeVerdict verdict) {
        if (isBlank(verdict.good()) || isBlank(verdict.improve()) || isBlank(verdict.insight())) {
            throw new NebiusRequestException("Judge returned incomplete feedback");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String readResource(String path) {
        try (InputStream in = ModelGrader.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + path, e);
        }
    }

    private static JsonNode readSchema(ObjectMapper objectMapper) {
        try (InputStream in = ModelGrader.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + SCHEMA_RESOURCE);
            }
            return objectMapper.readTree(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + SCHEMA_RESOURCE, e);
        }
    }
}
