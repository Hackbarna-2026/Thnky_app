package com.thnky.challenge;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thnky.domain.Challenge;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Lang;
import com.thnky.domain.Skill;

/**
 * Backed by {@code challenge_cache} (supabase/schema.sql). Active only when
 * {@code SUPABASE_DB_URL} is set — see {@link com.thnky.profile.SupabaseLearnerProfileRepository}
 * for why this is a property check rather than {@code @ConditionalOnBean(JdbcClient.class)}.
 */
@Component
@ConditionalOnExpression("!'${thnky.db.url:}'.isEmpty()")
class SupabaseChallengeCache implements ChallengeCache {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    SupabaseChallengeCache(JdbcClient jdbcClient, ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Challenge> get(Skill skill, Difficulty diff, Lang lang, String userId) {
        return jdbcClient.sql("select challenge::text from challenge_cache where key = :key")
                .param("key", buildKey(skill, diff, lang, userId))
                .query(String.class)
                .optional()
                .flatMap(this::parse);
    }

    @Override
    public void put(Skill skill, Difficulty diff, Lang lang, String userId, Challenge challenge) {
        jdbcClient.sql("""
                        insert into challenge_cache (key, challenge)
                        values (:key, :challenge::jsonb)
                        on conflict (key) do update set challenge = excluded.challenge, created_at = now()
                        """)
                .param("key", buildKey(skill, diff, lang, userId))
                .param("challenge", toJson(challenge))
                .update();
    }

    @Override
    public Optional<Challenge> findById(String id) {
        return jdbcClient.sql("select challenge::text from challenge_cache where challenge ->> 'id' = :id")
                .param("id", id)
                .query(String.class)
                .optional()
                .flatMap(this::parse);
    }

    private static String buildKey(Skill skill, Difficulty diff, Lang lang, String userId) {
        return String.join("|",
                skill.toJson(),
                diff == null ? "any" : diff.toJson(),
                lang == null ? "any" : lang.toJson(),
                userId == null ? "anon" : userId);
    }

    private Optional<Challenge> parse(String json) {
        try {
            return Optional.of(objectMapper.readValue(json, Challenge.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    private String toJson(Challenge challenge) {
        try {
            return objectMapper.writeValueAsString(challenge);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize challenge " + challenge.id(), e);
        }
    }
}
