package com.thnky.domain;

/** The response body of POST /api/answers. */
public record Verdict(
        boolean correct,
        int xp,
        String good,
        String improve,
        String insight
) {
}
