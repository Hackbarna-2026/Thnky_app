package com.thnky.profile;

/** One graded attempt, kept to summarize recent performance for a skill. */
public record AttemptResult(boolean correct, int hintsUsed, int seconds) {
}
