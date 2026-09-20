package com.thnky.ai;

/**
 * What the judge model returns — see {@code prompts/judge-verdict.schema.json}.
 * {@code onTopic} only matters when {@code correct} is false: it separates a
 * genuine wrong attempt from an answer that never engaged with the
 * challenge, which earn very different XP.
 */
public record JudgeVerdict(boolean correct, boolean onTopic, String good, String improve, String insight) {
}
