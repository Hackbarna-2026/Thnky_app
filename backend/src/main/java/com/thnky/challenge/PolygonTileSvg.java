package com.thnky.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds the "polygon with a row of dots" odd-one-out puzzle (the {@code odd}
 * static challenge) from parameters, not from a model — CLAUDE.md section 11.
 * Five tiles follow the rule "dots equal sides"; one, at {@code oddIndex},
 * does not. Nebius only ever chooses the polygon side counts, which tile
 * breaks the rule, and by how much; every coordinate below is fixed code.
 */
final class PolygonTileSvg {

    private static final double RADIUS = 17;
    private static final double CENTER_X = 30;
    private static final double CENTER_Y = 23;

    private PolygonTileSvg() {
    }

    record Puzzle(List<String> tiles, int oddIndex) {
    }

    static Puzzle build(List<Integer> sides, int oddIndex, int violationDelta) {
        List<String> tiles = new ArrayList<>(sides.size());
        for (int i = 0; i < sides.size(); i++) {
            int sideCount = sides.get(i);
            int dots = (i == oddIndex) ? sideCount + violationDelta : sideCount;
            tiles.add(tile(sideCount, dots));
        }
        return new Puzzle(tiles, oddIndex);
    }

    private static String tile(int sides, int dots) {
        StringBuilder points = new StringBuilder();
        for (int i = 0; i < sides; i++) {
            double angle = (2 * Math.PI * i) / sides - Math.PI / 2;
            double x = CENTER_X + RADIUS * Math.cos(angle);
            double y = CENTER_Y + RADIUS * Math.sin(angle);
            if (i > 0) {
                points.append(' ');
            }
            points.append(String.format(Locale.ROOT, "%.1f,%.1f", x, y));
        }

        StringBuilder dotsSvg = new StringBuilder();
        int start = 30 - (dots - 1) * 5;
        for (int i = 0; i < dots; i++) {
            dotsSvg.append("<circle cx=\"").append(start + i * 10)
                    .append("\" cy=\"51\" r=\"2.6\" fill=\"currentColor\"/>");
        }

        return "<svg viewBox=\"0 0 60 60\" aria-hidden=\"true\"><polygon points=\"" + points
                + "\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2.2\" stroke-linejoin=\"round\"/>"
                + dotsSvg + "</svg>";
    }
}
