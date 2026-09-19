package com.thnky.challenge;

/** The model replied, but what it produced does not validate against the type it was asked for. */
public class ChallengeGenerationException extends RuntimeException {

    public ChallengeGenerationException(String message) {
        super(message);
    }
}
