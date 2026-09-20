package com.thnky.ai;

import java.util.List;

/**
 * What the model returns for a shape-sequence puzzle: pattern parameters,
 * never SVG — see {@code prompts/shape-sequence-system.md} and
 * {@link ShapeSequenceSchema}.
 */
public record ShapeSequenceContent(
        String hook,
        String title,
        String desc,
        int rotationStep,
        boolean startSolid,
        int visibleSteps,
        List<String> hints,
        String good,
        String improve,
        String insight
) {
}
