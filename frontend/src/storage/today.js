const KEY = 'thnky:today'

function read() {
  try {
    const raw = localStorage.getItem(KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function write(value) {
  try {
    localStorage.setItem(KEY, JSON.stringify(value))
  } catch {
    // storage unavailable: today's challenge is re-fetched on the next load
  }
}

// { challenge, done, xp } saved for that day, or null.
export function loadToday(dateKey) {
  const saved = read()
  if (!saved || saved.date !== dateKey) return null
  return { challenge: saved.challenge, done: Boolean(saved.done), xp: saved.xp ?? 0 }
}

export function saveToday(dateKey, challenge) {
  write({ date: dateKey, challenge, done: false, xp: 0 })
}

// Only counts when the finished challenge is today's.
export function markTodayDone(dateKey, challengeId, xp) {
  const saved = read()
  if (saved && saved.date === dateKey && saved.challenge?.id === challengeId) {
    write({ ...saved, done: true, xp })
  }
}

// Forget today's challenge if it is this one, e.g. when the backend no longer has it.
export function dropToday(challengeId) {
  if (read()?.challenge?.id === challengeId) clearToday()
}

export function clearToday() {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // nothing to clear
  }
}
