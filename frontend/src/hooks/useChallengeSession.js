import { useCallback, useRef, useState } from 'react'
import { getNextChallenge } from '../api/client.js'
import { planForSkill, resolveSkill } from '../lib/today.js'

// The challenge the learner is working on. session is null when they are on a tab, otherwise:
//   { phase: 'loading' | 'error', skillKey }
//   { phase: 'challenge', challenge }
//   { phase: 'results', challenge, verdict, seconds, hintsUsed }
export default function useChallengeSession(profile) {
  const [session, setSession] = useState(null)
  // Ignore a slow response if the learner has already left or started something else.
  const latest = useRef(0)

  const openChallenge = useCallback((challenge) => {
    latest.current += 1
    setSession({ phase: 'challenge', challenge })
  }, [])

  const start = useCallback(
    async (skillKey) => {
      const mine = ++latest.current
      setSession({ phase: 'loading', skillKey })
      try {
        const skill = resolveSkill(skillKey, profile.skills)
        const plan = planForSkill(profile, skill, Math.floor(Math.random() * 1000))
        const challenge = await getNextChallenge({ ...plan, userId: profile.userId })
        if (mine === latest.current) setSession({ phase: 'challenge', challenge })
      } catch {
        if (mine === latest.current) setSession({ phase: 'error', skillKey })
      }
    },
    [profile],
  )

  const showResults = useCallback((results) => {
    setSession((current) => ({ ...current, phase: 'results', ...results }))
  }, [])

  const close = useCallback(() => {
    latest.current += 1
    setSession(null)
  }, [])

  return { session, openChallenge, start, showResults, close }
}
