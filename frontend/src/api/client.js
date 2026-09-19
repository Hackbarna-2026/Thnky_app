import { MOCK_CHALLENGE } from './mockChallenge.js'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// GET /api/challenges/next?skill=&diff=&lang=  ->  a full Challenge, hints included
export async function getNextChallenge({ skill, diff, lang } = {}) {
  if (USE_MOCK) return MOCK_CHALLENGE

  const params = new URLSearchParams()
  for (const [key, value] of Object.entries({ skill, diff, lang })) {
    if (value) params.set(key, value)
  }

  const res = await fetch(`/api/challenges/next?${params}`)
  if (!res.ok) throw new Error(`Challenge request failed (${res.status})`)
  return res.json()
}
