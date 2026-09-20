package com.thnky.profile;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thnky.domain.Skill;

/**
 * Persists the rolling per-skill result history in {@code learner_profile}
 * and logs every attempt in full in {@code attempt} (supabase/schema.sql).
 * Active only when {@code SUPABASE_DB_URL} is set — same condition as
 * {@link com.thnky.config.DatabaseConfig}. Gating on the property rather
 * than on {@code @ConditionalOnBean(JdbcClient.class)} matters here: this is
 * a plain {@code @Component}, and Spring evaluates component conditions
 * before auto-configuration (which is what creates {@link JdbcClient}) runs,
 * so a bean-presence check would always see it as missing.
 */
@Component
@ConditionalOnExpression("!'${thnky.db.url:}'.isEmpty()")
class SupabaseLearnerProfileRepository implements LearnerProfileRepository {

    private static final int MAX_HISTORY = 5;

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    SupabaseLearnerProfileRepository(JdbcClient jdbcClient, ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void recordAttempt(String userId, Skill skill, AttemptResult result) {
        logAttempt(userId, result);
        updateRollingResults(userId, skill, result);
    }

    @Override
    public List<AttemptResult> recentResults(String userId, Skill skill) {
        return jdbcClient.sql("select last_results::text from learner_profile where user_id = :userId and skill = :skill")
                .param("userId", userId)
                .param("skill", skill.toJson())
                .query(String.class)
                .optional()
                .map(this::parseResults)
                .orElseGet(List::of);
    }

    @Override
    public boolean hasAttempted(String userId, Skill skill, String challengeId) {
        return jdbcClient.sql("select 1 from attempt where user_id = :userId and challenge_id = :challengeId limit 1")
                .param("userId", userId)
                .param("challengeId", challengeId)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    private void logAttempt(String userId, AttemptResult result) {
        jdbcClient.sql("""
                        insert into attempt (user_id, challenge_id, correct, hints_used, seconds)
                        values (:userId, :challengeId, :correct, :hintsUsed, :seconds)
                        """)
                .param("userId", userId)
                .param("challengeId", result.challengeId())
                .param("correct", result.correct())
                .param("hintsUsed", result.hintsUsed())
                .param("seconds", result.seconds())
                .update();
    }

    private void updateRollingResults(String userId, Skill skill, AttemptResult result) {
        List<AttemptResult> updated = new ArrayList<>(recentResults(userId, skill));
        updated.add(result);
        while (updated.size() > MAX_HISTORY) {
            updated.remove(0);
        }
        jdbcClient.sql("""
                        insert into learner_profile (user_id, skill, last_results)
                        values (:userId, :skill, :lastResults::jsonb)
                        on conflict (user_id, skill)
                        do update set last_results = excluded.last_results
                        """)
                .param("userId", userId)
                .param("skill", skill.toJson())
                .param("lastResults", toJson(updated))
                .update();
    }

    private List<AttemptResult> parseResults(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<AttemptResult>>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String toJson(List<AttemptResult> results) {
        try {
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize attempt results", e);
        }
    }
}
