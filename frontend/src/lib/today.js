import { LANGUAGES } from '../constants/onboarding.js'

const MS_PER_DAY = 86_400_000
const KNOWN_LANGS = LANGUAGES.map((language) => language.key)

// Local calendar day, e.g. "2026-09-19". Today's challenge is fixed for that day.
export function todayKey(now = new Date()) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

// Same day, same pick; the next day rotates through the learner's skills and languages.
export function planToday(profile, key = todayKey()) {
  const [year, month, day] = key.split('-').map(Number)
  const dayNumber = Math.floor(Date.UTC(year, month - 1, day) / MS_PER_DAY)

  const skill = profile.skills[dayNumber % profile.skills.length]
  if (skill !== 'code') return { skill }

  // Profiles saved before the language list was cut may hold languages the backend rejects.
  const langs = profile.langs.filter((lang) => KNOWN_LANGS.includes(lang))
  return { skill, lang: langs.length ? langs[dayNumber % langs.length] : undefined }
}
