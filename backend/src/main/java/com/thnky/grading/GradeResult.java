package com.thnky.grading;

/**
 * What a grader decided, before XP enters the picture — {@link AnswerService}
 * adds that. {@code onTopic} only matters when {@code correct} is false: a
 * genuine but wrong attempt earns real partial credit, an answer that never
 * engaged with the challenge (off-topic, placeholder text) earns almost
 * none. Choice and lines answers are always on-topic — picking a given
 * option is a real attempt by construction, there is no "gibberish" choice.
 */
public record GradeResult(boolean correct, boolean onTopic, String good, String improve, String insight) {
}
