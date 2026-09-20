package com.thnky.challenge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.thnky.domain.Challenge;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Lang;
import com.thnky.domain.Skill;

/**
 * Tries Nebius; falls back to the static bank on any error, timeout, or
 * schema that does not validate — CLAUDE.md section 12: the demo has to
 * survive Nebius having a bad day.
 */
@Primary
@Component
public class FallbackChallengeSource implements ChallengeSource {

    private static final Logger log = LoggerFactory.getLogger(FallbackChallengeSource.class);

    private final NebiusChallengeSource primary;
    private final StaticChallengeSource fallback;

    public FallbackChallengeSource(NebiusChallengeSource primary, StaticChallengeSource fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public Challenge next(Skill skill, Difficulty diff, Lang lang, String userId) {
        try {
            return primary.next(skill, diff, lang, userId);
        } catch (RuntimeException e) {
            log.warn("Nebius generation failed for skill={}, diff={}, lang={} — falling back to static bank: {}",
                    skill, diff, lang, e.toString());
            return fallback.next(skill, diff, lang, userId);
        }
    }
}
