import { MOCK_CHALLENGE, mockVerdict } from './mockChallenge.js'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// GET /api/challenges/next?skill=&diff=&lang=&userId=  ->  a full Challenge, hints included.
// userId lets the backend adapt the challenge to the learner's recent results.
export async function getNextChallenge({ skill, diff, lang, userId } = {}) {
  if (USE_MOCK) return MOCK_CHALLENGE

  const params = new URLSearchParams()
  for (const [key, value] of Object.entries({ skill, diff, lang, userId })) {
    if (value) params.set(key, value)
  }

  const res = await fetch(`/api/challenges/next?${params}`)
  if (!res.ok) throw new Error(`Challenge request failed (${res.status})`)
  return res.json()
}

// POST /api/answers  ->  { correct, xp, good, improve, insight }
// answer is an index for choice and lines challenges, and text for text and code.
export async function submitAnswer({ challengeId, answer, hintsUsed, seconds, userId }) {
  if (USE_MOCK) return mockVerdict(answer)

  const res = await fetch('/api/answers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ challengeId, answer, hintsUsed, seconds, userId }),
  })
  if (!res.ok) throw new Error(`Answer request failed (${res.status})`)
  return res.json()
}
