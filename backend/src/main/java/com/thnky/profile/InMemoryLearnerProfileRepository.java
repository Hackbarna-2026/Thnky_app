package com.thnky.profile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.thnky.domain.Skill;

/**
 * Keeps the last few results per (userId, skill) in memory. Lost on restart —
 * fine for a hackathon demo, and swapped for a Supabase-backed
 * {@link LearnerProfileRepository} without touching any caller once the
 * {@code learner_profile} table is wired up (CLAUDE.md section 9).
 */
@Component
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
