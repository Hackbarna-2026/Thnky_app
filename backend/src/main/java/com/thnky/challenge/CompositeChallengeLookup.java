package com.thnky.challenge;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.thnky.domain.Challenge;

/**
 * Answers can be graded against a challenge from any source — the static
 * bank or a Nebius generation this instance served earlier. Tries each in
 * turn. Takes the concrete sources by type rather than autowiring
 * {@code List<ChallengeLookup>}: this class is itself a {@link ChallengeLookup}
 * bean, so a list-of-interface injection would include itself.
 */
@Primary
@Component
public class CompositeChallengeLookup implements ChallengeLookup {

    private final List<ChallengeLookup> lookups;

    public CompositeChallengeLookup(StaticChallengeSource staticSource, NebiusChallengeSource nebiusSource) {
        this.lookups = List.of(staticSource, nebiusSource);
    }

    @Override
    public Optional<Challenge> findById(String id) {
        return lookups.stream()
                .flatMap(lookup -> lookup.findById(id).stream())
                .findFirst();
    }
}
