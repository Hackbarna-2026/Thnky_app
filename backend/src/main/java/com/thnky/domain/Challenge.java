package com.thnky.domain;

import java.util.List;

/**
 * The one shape a challenge has, whether it comes from the static bank or
 * from Nebius. Also the JSON schema handed to the model and the object the
 * frontend consumes — see CLAUDE.md section 8.
 */
public record Challenge(
        String id,
        Skill skill,
        Lang lang,
        Difficulty diff,
        ChallengeType type,
        String hook,
        String title,
        String desc,

        List<String> options,
        Integer answer,
        List<String> optionsSvg,
        String figure,

        String starter,
        List<String> lines,
        String file,

        List<String> hints,
        String good,
        String improve,
        String insight
) {
}
