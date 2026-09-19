package com.thnky.challenge;

public class ChallengeNotFoundException extends RuntimeException {

    public ChallengeNotFoundException(String challengeId) {
        super("No challenge found for id: " + challengeId);
    }
}
