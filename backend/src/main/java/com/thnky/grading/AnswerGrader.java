package com.thnky.grading;

import com.thnky.domain.Answer;
import com.thnky.domain.Challenge;
import com.thnky.domain.ChallengeType;

/**
 * How a submitted answer is checked and turned into feedback. {@code IndexGrader}
 * handles choice and lines locally; {@code ModelGrader} judges text and code
 * with Nebius, in one call that returns the verdict and all three feedback
 * fields together — never both graders for the same {@link ChallengeType}.
 */
public interface AnswerGrader {

    boolean supports(ChallengeType type);

    GradeResult grade(Challenge challenge, Answer answer, int hintsUsed, int secondsSpent);
}
