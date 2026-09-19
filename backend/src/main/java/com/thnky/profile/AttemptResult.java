package com.thnky.profile;

/** One graded attempt, kept to summarize recent performance for a skill and, where persisted, logged in full. */
public record AttemptResult(String challengeId, boolean correct, int hintsUsed, int seconds) {
}
