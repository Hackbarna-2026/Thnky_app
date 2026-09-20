import { todayKey } from './today.js'

export const XP_PER_LEVEL = 100
const RECENT_ACTIVITY = 6

export const levelOf = (xp) => Math.floor(xp / XP_PER_LEVEL) + 1
export const xpIntoLevel = (xp) => xp % XP_PER_LEVEL
export const xpToNextLevel = (xp) => XP_PER_LEVEL - xpIntoLevel(xp)

function previousDay(key) {
  const [year, month, day] = key.split('-').map(Number)
  return todayKey(new Date(year, month - 1, day - 1))
}

// The streak to show. It survives until the day after the last challenge, then it is broken.
export function currentStreak(progress, key = todayKey()) {
  const alive = progress.lastActive === key || progress.lastActive === previousDay(key)
  return alive ? progress.streak : 0
}

// A new progress after one graded challenge. XP comes from the backend verdict.
export function applyResult(progress, { challenge, verdict }, key = todayKey()) {
  let streak = 1
  if (progress.lastActive === key) streak = progress.streak
  else if (progress.lastActive === previousDay(key)) streak = progress.streak + 1

  return {
    xp: progress.xp + verdict.xp,
    streak,
    lastActive: key,
    done: progress.done + 1,
    bySkill: { ...progress.bySkill, [challenge.skill]: (progress.bySkill[challenge.skill] ?? 0) + 1 },
    log: [
      { title: challenge.title, skill: challenge.skill, xp: verdict.xp, correct: verdict.correct },
      ...progress.log,
    ].slice(0, RECENT_ACTIVITY),
  }
}
