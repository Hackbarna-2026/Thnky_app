const KEY = 'thnky:profile'

// No real accounts: the profile lives in the browser and userId is an anonymous id.
export function loadProfile() {
  try {
    const raw = localStorage.getItem(KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function saveProfile(profile) {
  try {
    localStorage.setItem(KEY, JSON.stringify(profile))
  } catch {
    // storage unavailable (private mode): the profile lasts for this session only
  }
}

export function clearProfile() {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // nothing to clear
  }
}

export function newUserId() {
  return (
    globalThis.crypto?.randomUUID?.() ??
    `u-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`
  )
}
