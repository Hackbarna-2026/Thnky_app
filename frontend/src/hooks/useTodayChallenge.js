import { useCallback, useEffect, useState } from 'react'
import { getNextChallenge } from '../api/client.js'
import { planToday, todayKey } from '../lib/today.js'
import { loadToday, saveToday } from '../storage/today.js'

// The saved challenge counts only if it is today's and still matches a skill the learner practices.
function readSaved(profile) {
  const saved = loadToday(todayKey())
  return saved && profile.skills.includes(saved.challenge.skill) ? saved : null
}

const LOADING = { status: 'loading', challenge: null, done: false, xp: 0 }

// status: 'loading' | 'ready' | 'error'. done and xp tell whether today's challenge is finished.
export default function useTodayChallenge(profile) {
  const [state, setState] = useState(() => {
    const saved = readSaved(profile)
    return saved ? { status: 'ready', ...saved } : LOADING
  })
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    if (readSaved(profile)) return

    let cancelled = false
    setState(LOADING)
    getNextChallenge({ ...planToday(profile), userId: profile.userId })
      .then((challenge) => {
        if (cancelled) return
        saveToday(todayKey(), challenge)
        setState({ status: 'ready', challenge, done: false, xp: 0 })
      })
      .catch(() => !cancelled && setState({ ...LOADING, status: 'error' }))
    return () => {
      cancelled = true
    }
  }, [profile, attempt])

  const retry = useCallback(() => setAttempt((n) => n + 1), [])

  return { ...state, retry }
}
