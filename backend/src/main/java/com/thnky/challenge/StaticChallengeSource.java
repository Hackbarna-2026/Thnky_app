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

/**
 * The seeded bank from {@code challenges/bank.json}, loaded once at startup.
 * No network, cannot fail at request time — this is the fallback every other
 * {@link ChallengeSource} falls back to (CLAUDE.md section 12).
 */
@Component
public class StaticChallengeSource implements ChallengeSource, ChallengeLookup {

    private static final String BANK_RESOURCE = "/challenges/bank.json";

    private final List<Challenge> bank;

    public StaticChallengeSource(ObjectMapper objectMapper) {
        this.bank = loadBank(objectMapper);
    }

    @Override
    public Challenge next(Skill skill, Difficulty diff, Lang lang) {
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
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
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
