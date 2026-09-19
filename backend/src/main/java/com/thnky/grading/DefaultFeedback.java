package com.thnky.grading;

import com.thnky.domain.Challenge;

/**
 * Shared feedback composition for graders that reuse the challenge's own
 * authored copy rather than generating personalized text — {@link IndexGrader}
 * and {@link HeuristicAnswerGrader}. {@link ModelGrader} generates its own.
 */
final class DefaultFeedback {

    private static final String FALLBACK_GOOD =
            "You stayed with it instead of closing the tab. That is most of the habit.";
    private static final String FALLBACK_IMPROVE =
            "Try the next one with one hint fewer. You were closer than you thought.";

    private DefaultFeedback() {
    }

    static GradeResult compose(Challenge challenge, boolean correct, int hintsUsed) {
        String good = correct ? challenge.good() : FALLBACK_GOOD;
        String improve = (correct && hintsUsed >= 2) ? FALLBACK_IMPROVE : challenge.improve();
        return new GradeResult(correct, good, improve, challenge.insight());
    }
}
