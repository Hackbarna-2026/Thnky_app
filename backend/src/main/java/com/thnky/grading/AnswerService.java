package com.thnky.grading;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.thnky.challenge.ChallengeLookup;
import com.thnky.challenge.ChallengeNotFoundException;
import com.thnky.domain.Answer;
import com.thnky.domain.Challenge;
import com.thnky.domain.ChallengeType;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Verdict;
import com.thnky.profile.AttemptResult;
import com.thnky.profile.LearnerProfileRepository;

/** Orchestrates POST /api/answers: looks up the challenge, grades, adds XP, records the attempt. */
@Service
public class AnswerService {

    private static final int MIN_XP = 5;
    private static final int XP_PENALTY_PER_HINT = 5;
    private static final float WRONG_ANSWER_XP_SHARE = 0.35f;

    private final ChallengeLookup challengeLookup;
    private final AnswerGrader grader;
    private final LearnerProfileRepository profileRepository;

    public AnswerService(ChallengeLookup challengeLookup, AnswerGrader grader, LearnerProfileRepository profileRepository) {
        this.challengeLookup = challengeLookup;
        this.grader = grader;
        this.profileRepository = profileRepository;
    }

    public Verdict grade(String challengeId, JsonNode rawAnswer, int hintsUsed, int seconds, String userId) {
        Challenge challenge = challengeLookup.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException(challengeId));

        Answer answer = toAnswer(challenge.type(), rawAnswer);
        GradeResult result = grader.grade(challenge, answer, hintsUsed, seconds);
        int xp = computeXp(challenge.diff(), result.correct(), hintsUsed);

        if (userId != null && !userId.isBlank()) {
            profileRepository.recordAttempt(userId, challenge.skill(),
                    new AttemptResult(challenge.id(), result.correct(), hintsUsed, seconds));
        }

        return new Verdict(result.correct(), xp, result.good(), result.improve(), result.insight());
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
