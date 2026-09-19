package com.thnky.grading;

/** What a grader decided, before XP enters the picture — {@link AnswerService} adds that. */
public record GradeResult(boolean correct, String good, String improve, String insight) {
}
