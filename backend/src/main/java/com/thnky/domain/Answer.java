package com.thnky.domain;

/**
 * What the learner submitted. Choice and lines answers are an index into the
 * challenge's options; text and code answers are free-form.
 */
public sealed interface Answer {

    record Index(int value) implements Answer {
    }

    record Text(String value) implements Answer {
    }
}
