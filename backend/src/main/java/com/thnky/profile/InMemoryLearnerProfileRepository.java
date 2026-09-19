package com.thnky.profile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import com.thnky.domain.Skill;

/**
 * Keeps the last few results per (userId, skill) in memory. Lost on restart.
 * Active only when {@code SUPABASE_DB_URL} is unset, matching the app's
 * "works without a database" guarantee (CLAUDE.md section 12) — see
 * {@link SupabaseLearnerProfileRepository} for why this checks the property
 * rather than {@code @ConditionalOnMissingBean}.
 */
@Component
@ConditionalOnExpression("'${thnky.db.url:}'.isEmpty()")
public class InMemoryLearnerProfileRepository implements LearnerProfileRepository {

    private static final int MAX_HISTORY = 5;

    private final Map<String, List<AttemptResult>> resultsByKey = new ConcurrentHashMap<>();

    @Override
    public void recordAttempt(String userId, Skill skill, AttemptResult result) {
        List<AttemptResult> history = resultsByKey.computeIfAbsent(key(userId, skill), k -> new CopyOnWriteArrayList<>());
        history.add(result);
        while (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    @Override
    public List<AttemptResult> recentResults(String userId, Skill skill) {
        return List.copyOf(resultsByKey.getOrDefault(key(userId, skill), List.of()));
    }

    private static String key(String userId, Skill skill) {
        return userId + ":" + skill;
    }
}
