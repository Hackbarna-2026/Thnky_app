package com.thnky.challenge;

import com.thnky.domain.Challenge;
import com.thnky.domain.Difficulty;
import com.thnky.domain.Lang;
import com.thnky.domain.Skill;

/**
 * Where a challenge comes from. {@code diff} and {@code lang} are optional
 * filters, and {@code userId} is optional context for personalization —
 * {@code null} means "any" / "anonymous". Implementations either return a
 * valid challenge or throw {@link ChallengeUnavailableException}; never a
 * partial one.
 */
public interface ChallengeSource {

    Challenge next(Skill skill, Difficulty diff, Lang lang, String userId);
}
