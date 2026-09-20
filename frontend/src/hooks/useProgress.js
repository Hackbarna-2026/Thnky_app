import { useCallback, useState } from 'react'
import { applyResult, currentStreak, levelOf } from '../lib/progress.js'
import { EMPTY_PROGRESS, clearProgress, loadProgress, saveProgress } from '../storage/progress.js'

// The learner's XP, level and streak, kept in localStorage.
export default function useProgress() {
  const [progress, setProgress] = useState(loadProgress)

  // Applies a graded challenge and returns the new stored progress.
  const record = useCallback(
    (challenge, verdict) => {
      const next = applyResult(progress, { challenge, verdict })
      saveProgress(next)
      setProgress(next)
      return next
    },
    [progress],
  )

  const reset = useCallback(() => {
    clearProgress()
    setProgress(EMPTY_PROGRESS)
  }, [])

  return {
    xp: progress.xp,
    level: levelOf(progress.xp),
    streak: currentStreak(progress),
    done: progress.done,
    bySkill: progress.bySkill,
    log: progress.log,
    record,
    reset,
  }
}
