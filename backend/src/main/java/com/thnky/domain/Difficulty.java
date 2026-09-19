package com.thnky.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Difficulty {
    EASY("Easy"), MEDIUM("Medium"), HARD("Hard");

    private final String label;

    Difficulty(String label) {
        this.label = label;
    }

    @JsonValue
    public String toJson() {
        return label;
    }

    @JsonCreator
    public static Difficulty parse(String value) {
        for (Difficulty d : values()) {
            if (d.label.equalsIgnoreCase(value.trim())) {
                return d;
            }
        }
        throw new InvalidChallengeParamsException("Unknown difficulty: " + value);
    }
}
