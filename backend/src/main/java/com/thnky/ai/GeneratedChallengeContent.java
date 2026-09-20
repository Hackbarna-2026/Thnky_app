package com.thnky.ai;

import java.util.List;

/**
 * What the generator model actually produces — see
 * {@code prompts/generated-challenge.schema.json}. Deliberately narrower than
 * {@link com.thnky.domain.Challenge}: id, skill, lang, diff and type are
 * request parameters, not generated content, and figure/optionsSvg are never
 * requested from the model (CLAUDE.md section 11 — no free-form SVG from an LLM).
 */
public record GeneratedChallengeContent(
        String hook,
        String title,
        String desc,
        List<String> options,
        Integer answer,
        String starter,
        List<String> lines,
        String file,
        List<String> hints,
        String good,
        String improve,
        String insight
) {
}
