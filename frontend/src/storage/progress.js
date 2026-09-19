const KEY = 'thnky:progress'
const DEFAULT = { xp: 0, streak: 0 }

export const levelOf = (xp) => Math.floor(xp / 100) + 1

export function loadProgress() {
  try {
    const raw = localStorage.getItem(KEY)
    return raw ? { ...DEFAULT, ...JSON.parse(raw) } : { ...DEFAULT }
  } catch {
    return { ...DEFAULT }
  }
}

export function clearProgress() {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // nothing to clear
  }
}
