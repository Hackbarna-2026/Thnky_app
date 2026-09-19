package com.thnky.challenge;

import org.springframework.stereotype.Service;

import com.thnky.domain.Challenge;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Lang;
import com.thnky.domain.Skill;

/**
 * Orchestrates challenge retrieval on behalf of the controller. Today it is a
 * plain delegate to the static bank; once {@code NebiusChallengeSource} and
 * its decorators exist, this is where they get wired in without the
 * controller or this class's public contract changing.
 */
@Service
public class ChallengeService {

    private final ChallengeSource challengeSource;

    public ChallengeService(ChallengeSource challengeSource) {
        this.challengeSource = challengeSource;
    }

    public Challenge next(Skill skill, Difficulty diff, Lang lang) {
        return challengeSource.next(skill, diff, lang);
    }
}
