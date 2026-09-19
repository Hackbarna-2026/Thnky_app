package com.thnky.profile;

import java.util.List;

import com.thnky.domain.Skill;

/**
 * The learner's recent results, per skill — CLAUDE.md section 10: "para cada
 * skill, los últimos resultados". Backed by memory today; a Supabase-backed
 * implementation of this same interface is what step 6 (persistence) adds.
 */
public interface LearnerProfileRepository {

    void recordAttempt(String userId, Skill skill, AttemptResult result);

    List<AttemptResult> recentResults(String userId, Skill skill);
}
