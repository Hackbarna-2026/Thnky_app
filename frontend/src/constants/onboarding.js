// Must match the backend's Lang enum.
export const LANGUAGES = [
  { key: 'js', name: 'JavaScript' },
  { key: 'py', name: 'Python' },
  { key: 'sql', name: 'SQL' },
  { key: 'java', name: 'Java' },
  { key: 'typescript', name: 'TypeScript' },
  { key: 'cpp', name: 'C++' },
  { key: 'ruby', name: 'Ruby' },
]

export const LEVELS = [
  { key: 'beginner', label: 'Beginner' },
  { key: 'comfortable', label: 'Comfortable' },
  { key: 'advanced', label: 'Advanced' },
]
export const DEFAULT_LEVEL = 'comfortable'

// The onboarding level becomes the `diff` sent to GET /api/challenges/next.
export const DIFFICULTY_BY_LEVEL = {
  beginner: 'Easy',
  comfortable: 'Medium',
  advanced: 'Hard',
}

export const DAILY_MINUTES = [3, 5, 10]
export const DEFAULT_MINUTES = 5

export const INTRO_SCREENS = [
  { icon: 'spark', tone: 'accent', text: 'AI can do more for us than ever.' },
  { icon: 'bulb', tone: 'hot', text: 'But some things are worth keeping sharp.' },
  { icon: 'bolt', tone: 'cool', text: 'Thnky gives you a little challenge every day.' },
]
