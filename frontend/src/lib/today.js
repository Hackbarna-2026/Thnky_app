import { DIFFICULTY_BY_LEVEL, LANGUAGES } from '../constants/onboarding.js'

const MS_PER_DAY = 86_400_000
const KNOWN_LANGS = LANGUAGES.map((language) => language.key)

// Local calendar day, e.g. "2026-09-19". Today's challenge is fixed for that day.
export function todayKey(now = new Date()) {
  const pad = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

// What to ask the backend for a given skill. The difficulty follows the level the learner chose
// for it. For coding, `pick` rotates through their languages.
export function planForSkill(profile, skill, pick = 0) {
  const diff = DIFFICULTY_BY_LEVEL[profile.levels?.[skill]]
  if (skill !== 'code') return { skill, diff }

  // Profiles saved before the language list was cut may hold languages the backend rejects.
  const langs = profile.langs.filter((lang) => KNOWN_LANGS.includes(lang))
  return { skill, diff, lang: langs.length ? langs[pick % langs.length] : undefined }
}

// Same day, same pick; the next day rotates through the learner's skills and languages.
export function planToday(profile, key = todayKey()) {
  const [year, month, day] = key.split('-').map(Number)
  const dayNumber = Math.floor(Date.UTC(year, month - 1, day) / MS_PER_DAY)
  const skill = profile.skills[dayNumber % profile.skills.length]
  return planForSkill(profile, skill, dayNumber)
}

// "Surprise me" is not a backend skill: pick one of the learner's own.
export function resolveSkill(key, skills) {
  return key === 'surprise' ? skills[Math.floor(Math.random() * skills.length)] : key
}
