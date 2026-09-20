package com.thnky.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Lang {
    JS("JavaScript"), PY("Python"), SQL("SQL"),
    JAVA("Java"), TYPESCRIPT("TypeScript"), CPP("C++"), RUBY("Ruby");

    private final String displayName;

    Lang(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    public String displayName() {
        return displayName;
    }

    @JsonCreator
    public static Lang parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidChallengeParamsException("Unknown lang: " + value);
        }
    }
}
