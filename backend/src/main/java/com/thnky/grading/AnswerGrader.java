package com.thnky.grading;

import com.thnky.domain.Answer;
import com.thnky.domain.Challenge;
import com.thnky.domain.ChallengeType;

/**
 * How a submitted answer is checked. {@code IndexGrader} handles choice and
 * lines locally; the model-backed grader for text and code arrives with
 * Nebius. Never both at once for the same {@link ChallengeType}.
 */
public interface AnswerGrader {

    boolean supports(ChallengeType type);

    boolean isCorrect(Challenge challenge, Answer answer);
}
