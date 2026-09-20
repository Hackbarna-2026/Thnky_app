// Holds the one in-flight "today's challenge" request started during onboarding, keyed by
// planKey() (see lib/today.js). A module-level singleton rather than component state: it has
// to survive OnboardingScreen unmounting and HomeScreen mounting in its place.
let pending = null

export function startTodayPrefetch(key, run) {
  const promise = run()
  promise.catch(() => {}) // a failed prefetch is not an error: whoever takes it will retry
  pending = { key, promise }
}

// Returns the pending promise if it was started for this exact plan, otherwise null — a
// mismatch (skills changed after the prefetch, a new day started) means "no, fetch fresh."
export function takeTodayPrefetch(key) {
  if (pending?.key !== key) return null
  const { promise } = pending
  pending = null
  return promise
}
