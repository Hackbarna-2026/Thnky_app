package com.thnky.grading;

import org.springframework.stereotype.Component;

import com.thnky.domain.Answer;
import com.thnky.domain.Challenge;
import com.thnky.domain.ChallengeType;

/** Choice and lines: compares the submitted index against the stored one. Instant, free, never wrong. */
@Component
public class IndexGrader implements AnswerGrader {

    @Override
    public boolean supports(ChallengeType type) {
        return type == ChallengeType.CHOICE || type == ChallengeType.LINES;
    }

    @Override
    public boolean isCorrect(Challenge challenge, Answer answer) {
        if (!(answer instanceof Answer.Index index)) {
            return false;
        }
        Integer expected = challenge.answer();
        return expected != null && expected == index.value();
    }
}
