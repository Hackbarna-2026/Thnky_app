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
 * Local fallback for text and code grading, used when {@link ModelGrader}
 * fails — same word-count-and-keyword rule the prototype checks
 * client-side. Free, instant, never calls out to anything, so it always
 * works even if Nebius is down (CLAUDE.md section 12).
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
    public GradeResult grade(Challenge challenge, Answer answer, int hintsUsed, int secondsSpent) {
        boolean correct = isCorrect(challenge, answer);
        return DefaultFeedback.compose(challenge, correct, hintsUsed);
    }

    private boolean isCorrect(Challenge challenge, Answer answer) {
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
