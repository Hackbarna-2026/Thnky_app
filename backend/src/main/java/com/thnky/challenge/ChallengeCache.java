package com.thnky.challenge;

import java.util.Optional;

import com.thnky.domain.Challenge;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Lang;
import com.thnky.domain.Skill;

/**
 * Caches Nebius-generated challenges by request signature, so the same
 * skill/diff/lang/learner does not pay for a second generation call
 * (CLAUDE.md section 12), and lets a generated challenge be found by id
 * again after a restart, for grading.
 */
public interface ChallengeCache {

    Optional<Challenge> get(Skill skill, Difficulty diff, Lang lang, String userId);

    void put(Skill skill, Difficulty diff, Lang lang, String userId, Challenge challenge);

    Optional<Challenge> findById(String id);
}
