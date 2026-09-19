package com.thnky.grading;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thnky.domain.Answer;
import com.thnky.domain.Challenge;
import com.thnky.domain.ChallengeType;

/**
 * Local stand-in for text and code grading, used while there is no Nebius
 * judge yet. Same word-count-and-keyword rule the prototype checks
 * client-side. Replaced wholesale by a model-backed grader once Nebius is
 * wired — the {@link AnswerGrader} contract does not change.
 */
@Component
public class HeuristicAnswerGrader implements AnswerGrader {

    private static final String CRITERIA_RESOURCE = "/challenges/heuristic-criteria.json";
    private static final int MIN_TEXT_WORDS = 5;
    private static final int MIN_CODE_EXTRA_LENGTH = 25;

    private final Map<String, HeuristicCriteria> criteriaById;

    public HeuristicAnswerGrader(ObjectMapper objectMapper) {
        this.criteriaById = loadCriteria(objectMapper);
    }

    @Override
    public boolean supports(ChallengeType type) {
        return type == ChallengeType.TEXT || type == ChallengeType.CODE;
    }

    @Override
    public boolean isCorrect(Challenge challenge, Answer answer) {
        if (!(answer instanceof Answer.Text text)) {
            return false;
        }
        HeuristicCriteria criteria = criteriaById.get(challenge.id());
        if (criteria == null) {
            return false;
        }
        String value = text.value().trim();
        return challenge.type() == ChallengeType.CODE
                ? isCorrectCode(challenge, criteria, value)
                : isCorrectText(criteria, value);
    }

    private boolean isCorrectCode(Challenge challenge, HeuristicCriteria criteria, String value) {
        String starter = challenge.starter() == null ? "" : challenge.starter();
        if (value.replaceAll("\\s", "").equals(starter.replaceAll("\\s", ""))) {
            return false;
        }
        if (value.length() <= starter.length() + MIN_CODE_EXTRA_LENGTH) {
            return false;
        }
        String lower = value.toLowerCase();
        return criteria.keywords().stream().allMatch(lower::contains);
    }

    private boolean isCorrectText(HeuristicCriteria criteria, String value) {
        long wordCount = Arrays.stream(value.split("\\s+")).filter(w -> !w.isBlank()).count();
        if (wordCount < MIN_TEXT_WORDS) {
            return false;
        }
        if (criteria.minWords() != null && wordCount < criteria.minWords()) {
            return false;
        }
        String lower = value.toLowerCase();
        return criteria.keywords().stream().anyMatch(lower::contains);
    }

    private static Map<String, HeuristicCriteria> loadCriteria(ObjectMapper objectMapper) {
        try (InputStream in = HeuristicAnswerGrader.class.getResourceAsStream(CRITERIA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + CRITERIA_RESOURCE);
            }
            return objectMapper.readValue(in, new TypeReference<Map<String, HeuristicCriteria>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not load heuristic grading criteria", e);
        }
    }
}
