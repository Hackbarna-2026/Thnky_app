package com.thnky.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thnky.challenge.ChallengeService;
import com.thnky.domain.Challenge;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Lang;
import com.thnky.domain.Skill;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping("/next")
    public Challenge next(
            @RequestParam String skill,
            @RequestParam(required = false) String diff,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String userId
    ) {
        return challengeService.next(
                Skill.parse(skill),
                diff == null ? null : Difficulty.parse(diff),
                lang == null ? null : Lang.parse(lang),
                userId
        );
    }
}
