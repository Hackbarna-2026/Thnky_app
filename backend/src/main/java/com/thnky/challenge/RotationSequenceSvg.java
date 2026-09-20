package com.thnky.challenge;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the "rotating, toggling triangle" sequence puzzle (the {@code seq}
 * static challenge) from parameters instead of letting a model write SVG —
 * CLAUDE.md section 11. Nebius only ever chooses {@code rotationStep},
 * {@code startSolid} and {@code visibleSteps}; every path and coordinate
 * below is fixed code.
 *
 * {@code rotationStep} is restricted to 60/90 by {@link com.thnky.ai.ShapeSequenceSchema}:
 * a step of 180 makes the "previous step" and "next step" wrong-answer tiles
 * mathematically identical (rotation repeats every two steps), which would
 * make the multiple-choice question unanswerable.
 */
final class RotationSequenceSvg {

    private static final String TRIANGLE_PATH =
            "<path d=\"M30 11 L47 41 L13 41 Z\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.4\" "
                    + "stroke-linejoin=\"round\" transform=\"rotate(%d 30 30)\"/>";
    private static final String DOT =
            "<circle cx=\"30\" cy=\"31\" r=\"4.6\" stroke=\"currentColor\" stroke-width=\"2.2\" fill=\"%s\"/>";

    private RotationSequenceSvg() {
    }

    record Options(List<String> svgs, int correctIndex) {
    }

    static String figure(int rotationStep, boolean startSolid, int visibleSteps) {
        StringBuilder svg = new StringBuilder("<svg viewBox=\"0 0 ")
                .append((visibleSteps + 1) * 60).append(" 60\" aria-hidden=\"true\">");
        for (int step = 0; step < visibleSteps; step++) {
            svg.append("<g transform=\"translate(").append(step * 60).append(",0)\">")
                    .append(tileInner(rotationAt(rotationStep, step), solidAt(startSolid, step)))
                    .append("</g>");
        }
        int questionMarkX = visibleSteps * 60 + 30;
        svg.append("<text x=\"").append(questionMarkX).append("\" y=\"38\" text-anchor=\"middle\" font-size=\"26\" ")
                .append("font-family=\"Bricolage Grotesque, sans-serif\" fill=\"currentColor\">?</text>")
                .append("</svg>");
        return svg.toString();
    }

    /** The correct next tile plus three systematically wrong ones, in a fixed but shuffled order. */
    static Options options(int rotationStep, boolean startSolid, int visibleSteps, long shuffleSeed) {
        int correctRotation = rotationAt(rotationStep, visibleSteps);
        boolean correctSolid = solidAt(startSolid, visibleSteps);
        int previousRotation = rotationAt(rotationStep, visibleSteps - 1);
        int nextRotation = rotationAt(rotationStep, visibleSteps + 1);

        List<int[]> pairs = new ArrayList<>(List.of(
                new int[]{correctRotation, boolToInt(correctSolid)},
                new int[]{correctRotation, boolToInt(!correctSolid)},
                new int[]{previousRotation, boolToInt(correctSolid)},
                new int[]{nextRotation, boolToInt(correctSolid)}
        ));

        List<Integer> order = new ArrayList<>(List.of(0, 1, 2, 3));
        java.util.Collections.shuffle(order, new java.util.Random(shuffleSeed));

        List<String> svgs = new ArrayList<>(4);
        int correctIndex = 0;
        for (int i = 0; i < order.size(); i++) {
            int[] pair = pairs.get(order.get(i));
            svgs.add(tile(pair[0], pair[1] == 1));
            if (order.get(i) == 0) {
                correctIndex = i;
            }
        }
        return new Options(svgs, correctIndex);
    }

    private static int rotationAt(int rotationStep, int step) {
        return ((rotationStep * step) % 360 + 360) % 360;
    }

    private static boolean solidAt(boolean startSolid, int step) {
        return step % 2 == 0 ? startSolid : !startSolid;
    }

    private static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    private static String tile(int rotationDeg, boolean solid) {
        return "<svg viewBox=\"0 0 60 60\" aria-hidden=\"true\">" + tileInner(rotationDeg, solid) + "</svg>";
    }

    private static String tileInner(int rotationDeg, boolean solid) {
        return TRIANGLE_PATH.formatted(rotationDeg) + DOT.formatted(solid ? "currentColor" : "none");
    }
}
