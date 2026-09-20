const KEY = 'thnky:progress'

export const EMPTY_PROGRESS = {
  xp: 0,
  streak: 0,
  lastActive: null,
  done: 0,
  bySkill: { code: 0, logic: 0, critical: 0 },
  log: [],
}

export function loadProgress() {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return EMPTY_PROGRESS
    const saved = JSON.parse(raw)
    return { ...EMPTY_PROGRESS, ...saved, bySkill: { ...EMPTY_PROGRESS.bySkill, ...saved.bySkill } }
  } catch {
    return EMPTY_PROGRESS
  }
}

export function saveProgress(progress) {
  try {
    localStorage.setItem(KEY, JSON.stringify(progress))
  } catch {
    // storage unavailable: progress lasts for this session only
  }
}

export function clearProgress() {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // nothing to clear
  }
}
