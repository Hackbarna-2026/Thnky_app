package com.thnky.ai;

/** What the judge model returns — see {@code prompts/judge-verdict.schema.json}. */
public record JudgeVerdict(boolean correct, String good, String improve, String insight) {
}
