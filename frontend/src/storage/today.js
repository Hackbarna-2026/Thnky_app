const KEY = 'thnky:today'

export function loadToday(dateKey) {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return null
    const saved = JSON.parse(raw)
    return saved.date === dateKey ? saved.challenge : null
  } catch {
    return null
  }
}

export function saveToday(dateKey, challenge) {
  try {
    localStorage.setItem(KEY, JSON.stringify({ date: dateKey, challenge }))
  } catch {
    // storage unavailable: the challenge is re-fetched on the next load
  }
}

export function clearToday() {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // nothing to clear
  }
}
