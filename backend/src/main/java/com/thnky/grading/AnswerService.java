package com.thnky.grading;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.thnky.challenge.ChallengeLookup;
import com.thnky.challenge.ChallengeNotFoundException;
import com.thnky.domain.Answer;
import com.thnky.domain.Challenge;
import com.thnky.domain.ChallengeType;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Verdict;

/** Orchestrates POST /api/answers: looks up the challenge, grades, composes the verdict. */
@Service
public class AnswerService {

    private static final String FALLBACK_GOOD =
            "You stayed with it instead of closing the tab. That is most of the habit.";
    private static final String FALLBACK_IMPROVE =
            "Try the next one with one hint fewer. You were closer than you thought.";
    private static final int MIN_XP = 5;
    private static final int XP_PENALTY_PER_HINT = 5;
    private static final float WRONG_ANSWER_XP_SHARE = 0.35f;

    private final ChallengeLookup challengeLookup;
    private final List<AnswerGrader> graders;

    public AnswerService(ChallengeLookup challengeLookup, List<AnswerGrader> graders) {
        this.challengeLookup = challengeLookup;
        this.graders = graders;
    }

    public Verdict grade(String challengeId, JsonNode rawAnswer, int hintsUsed, int seconds) {
        Challenge challenge = challengeLookup.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException(challengeId));

        Answer answer = toAnswer(challenge.type(), rawAnswer);
        AnswerGrader grader = graders.stream()
                .filter(g -> g.supports(challenge.type()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No grader registered for type " + challenge.type()));

        boolean correct = grader.isCorrect(challenge, answer);
        int xp = computeXp(challenge.diff(), correct, hintsUsed);
        String good = correct ? challenge.good() : FALLBACK_GOOD;
        String improve = (correct && hintsUsed >= 2) ? FALLBACK_IMPROVE : challenge.improve();

        return new Verdict(correct, xp, good, improve, challenge.insight());
    }

    private Answer toAnswer(ChallengeType type, JsonNode raw) {
        boolean isIndexType = type == ChallengeType.CHOICE || type == ChallengeType.LINES;
        if (raw == null || raw.isNull()) {
            return isIndexType ? new Answer.Index(-1) : new Answer.Text("");
        }
        return isIndexType ? new Answer.Index(raw.asInt(-1)) : new Answer.Text(raw.asText(""));
    }

    private int computeXp(Difficulty diff, boolean correct, int hintsUsed) {
        int base = switch (diff) {
            case EASY -> 30;
            case MEDIUM -> 45;
            case HARD -> 60;
        };
        int xp = correct ? base : Math.round(base * WRONG_ANSWER_XP_SHARE);
        return Math.max(MIN_XP, xp - hintsUsed * XP_PENALTY_PER_HINT);
    }
}
