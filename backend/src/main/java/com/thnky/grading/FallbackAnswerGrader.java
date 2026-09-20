package com.thnky.grading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.thnky.domain.Answer;
import com.thnky.domain.Challenge;
import com.thnky.domain.ChallengeType;

/**
 * The single entry point {@link AnswerService} grades through: index-based
 * types go straight to {@link IndexGrader}; text and code go to
 * {@link ModelGrader}, falling back to {@link HeuristicAnswerGrader} on any
 * failure so a Nebius outage degrades a submission, never breaks it
 * (CLAUDE.md section 12).
 */
@Primary
@Component
public class FallbackAnswerGrader implements AnswerGrader {

    private static final Logger log = LoggerFactory.getLogger(FallbackAnswerGrader.class);

    private final IndexGrader indexGrader;
    private final ModelGrader modelGrader;
    private final HeuristicAnswerGrader heuristicGrader;

    public FallbackAnswerGrader(IndexGrader indexGrader, ModelGrader modelGrader, HeuristicAnswerGrader heuristicGrader) {
        this.indexGrader = indexGrader;
        this.modelGrader = modelGrader;
        this.heuristicGrader = heuristicGrader;
    }

    @Override
    public boolean supports(ChallengeType type) {
        return true;
    }

    @Override
    public GradeResult grade(Challenge challenge, Answer answer, int hintsUsed, int secondsSpent) {
        if (indexGrader.supports(challenge.type())) {
            return indexGrader.grade(challenge, answer, hintsUsed, secondsSpent);
        }
        try {
            return modelGrader.grade(challenge, answer, hintsUsed, secondsSpent);
        } catch (RuntimeException e) {
            log.warn("Nebius grading failed for challenge {} — falling back to the local heuristic: {}",
                    challenge.id(), e.toString());
            return heuristicGrader.grade(challenge, answer, hintsUsed, secondsSpent);
        }
    }
}
