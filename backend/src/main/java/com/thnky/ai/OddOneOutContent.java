package com.thnky.ai;

import java.util.List;

/**
 * What the model returns for an odd-one-out puzzle: polygon side counts and
 * which tile breaks the pattern, never SVG — see {@code prompts/odd-one-out-system.md}
 * and {@link OddOneOutSchema}.
 */
public record OddOneOutContent(
        String hook,
        String title,
        String desc,
        List<Integer> sides,
        int oddIndex,
        int violationDelta,
        List<String> hints,
        String good,
        String improve,
        String insight
) {
}
