package com.thnky.challenge;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thnky.domain.Challenge;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Lang;
import com.thnky.domain.Skill;
import com.thnky.profile.LearnerProfileRepository;

/**
 * The seeded bank from {@code challenges/bank.json}, loaded once at startup.
 * No network, cannot fail at request time — this is the fallback every other
 * {@link ChallengeSource} falls back to (CLAUDE.md section 12).
 */
@Component
public class StaticChallengeSource implements ChallengeSource, ChallengeLookup {

    private static final String BANK_RESOURCE = "/challenges/bank.json";

    private final List<Challenge> bank;
    private final LearnerProfileRepository profileRepository;

    public StaticChallengeSource(ObjectMapper objectMapper, LearnerProfileRepository profileRepository) {
        this.bank = loadBank(objectMapper);
        this.profileRepository = profileRepository;
    }

    @Override
    public Challenge next(Skill skill, Difficulty diff, Lang lang, String userId) {
        List<Challenge> candidates = filter(skill, diff, lang);
        if (candidates.isEmpty()) {
            candidates = filter(skill, diff, null);
        }
        if (candidates.isEmpty()) {
            candidates = filter(skill, null, null);
        }
        if (candidates.isEmpty()) {
            throw new ChallengeUnavailableException("No static challenge for skill: " + skill);
        }
        // Some skill/diff/lang combinations have only one or two static
        // entries. Without this, whichever one the learner already answered
        // keeps coming back — especially visible when Nebius is unreachable
        // and every request falls all the way back to this bank.
        List<Challenge> unseen = excludeAttempted(candidates, skill, userId);
        List<Challenge> pool = unseen.isEmpty() ? candidates : unseen;
        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    private List<Challenge> excludeAttempted(List<Challenge> candidates, Skill skill, String userId) {
        if (userId == null || userId.isBlank()) {
            return candidates;
        }
        return candidates.stream()
                .filter(c -> !profileRepository.hasAttempted(userId, skill, c.id()))
                .toList();
    }

    @Override
    public Optional<Challenge> findById(String id) {
        return bank.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    /** The full seeded bank, e.g. to pick few-shot examples for the Nebius generator prompt. */
    public List<Challenge> all() {
        return bank;
    }

    private List<Challenge> filter(Skill skill, Difficulty diff, Lang lang) {
        return bank.stream()
                .filter(c -> c.skill() == skill)
                .filter(c -> diff == null || c.diff() == diff)
                .filter(c -> lang == null || c.lang() == lang)
                .toList();
    }

    private static List<Challenge> loadBank(ObjectMapper objectMapper) {
        try (InputStream in = StaticChallengeSource.class.getResourceAsStream(BANK_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + BANK_RESOURCE);
            }
            return objectMapper.readValue(in, new TypeReference<List<Challenge>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not load the static challenge bank", e);
        }
    }
}
