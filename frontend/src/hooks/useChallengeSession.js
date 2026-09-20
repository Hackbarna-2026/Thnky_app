import { useCallback, useEffect, useRef, useState } from 'react'
import { getNextChallenge } from '../api/client.js'
import { planForSkill, resolveSkill } from '../lib/today.js'

// The challenge the learner is working on. session is null when they are on a tab, otherwise:
//   { phase: 'loading' | 'error', skillKey }
//   { phase: 'challenge', challenge, notice? }
//   { phase: 'results', challenge, verdict, seconds, hintsUsed, leveledUp }
export default function useChallengeSession(profile) {
  const [session, setSession] = useState(null)
  // Ignore a slow response if the learner has already left or started something else.
  const latest = useRef(0)
  // The next challenge, requested while the learner solves the current one.
  const prefetched = useRef(null)
  const userId = profile?.userId

  useEffect(() => {
    prefetched.current = null
  }, [userId])

  const request = useCallback(
    (skill) => {
      const plan = planForSkill(profile, skill, Math.floor(Math.random() * 1000))
      return getNextChallenge({ ...plan, userId: profile.userId })
    },
    [profile],
  )

  const prefetch = useCallback(
    (skill) => {
      const promise = request(skill)
      promise.catch(() => {}) // a failed prefetch is not an error: the next start asks again
      prefetched.current = { skill, promise }
    },
    [request],
  )

  // The prefetched challenge if it is for this skill and arrived fine, otherwise a fresh request.
  const take = useCallback(
    async (skill) => {
      const held = prefetched.current
      prefetched.current = null
      if (held?.skill === skill) {
        try {
          return await held.promise
        } catch {
          // fall through to a fresh request
        }
      }
      return request(skill)
    },
    [request],
  )

  const openChallenge = useCallback(
    (challenge) => {
      latest.current += 1
      setSession({ phase: 'challenge', challenge })
      prefetch(challenge.skill)
    },
    [prefetch],
  )

  const start = useCallback(
    async (skillKey, notice) => {
      const mine = ++latest.current
      setSession({ phase: 'loading', skillKey })
      try {
        const challenge = await take(resolveSkill(skillKey, profile.skills))
        if (mine !== latest.current) return
        setSession({ phase: 'challenge', challenge, notice })
        prefetch(challenge.skill)
      } catch {
        if (mine === latest.current) setSession({ phase: 'error', skillKey })
      }
    },
    [profile, take, prefetch],
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
