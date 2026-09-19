package com.thnky.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thnky.api.dto.AnswerRequest;
import com.thnky.domain.Verdict;
import com.thnky.grading.AnswerService;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping
    public Verdict submit(@RequestBody AnswerRequest request) {
        return answerService.grade(
                request.challengeId(), request.answer(), request.hintsUsed(), request.seconds(), request.userId());
    }
}
