package com.thnky.challenge;

import java.util.Optional;

import com.thnky.domain.Challenge;

/**
 * Retrieves a previously served challenge by id, so an answer can be graded
 * against it. Kept separate from {@link ChallengeSource}: a generative source
 * does not necessarily hand out challenges to pick "next" and look up later
 * the same way.
 */
public interface ChallengeLookup {

    Optional<Challenge> findById(String id);
}
