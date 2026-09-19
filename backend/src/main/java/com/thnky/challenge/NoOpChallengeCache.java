package com.thnky.challenge;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import com.thnky.domain.Challenge;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Lang;
import com.thnky.domain.Skill;

/** No database configured: every request is a cache miss, nothing is stored. */
@Component
@ConditionalOnExpression("'${thnky.db.url:}'.isEmpty()")
class NoOpChallengeCache implements ChallengeCache {

    @Override
    public Optional<Challenge> get(Skill skill, Difficulty diff, Lang lang, String userId) {
        return Optional.empty();
    }

    @Override
    public void put(Skill skill, Difficulty diff, Lang lang, String userId, Challenge challenge) {
        // nothing to persist without a database
    }

    @Override
    public Optional<Challenge> findById(String id) {
        return Optional.empty();
    }
}
