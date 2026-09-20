import { useCallback, useState } from 'react'
import { clearProfile, loadProfile, newUserId, saveProfile } from '../storage/profile.js'
import { clearToday } from '../storage/today.js'

// profile is null until the onboarding is completed.
export default function useProfile() {
  const [profile, setProfile] = useState(loadProfile)

  const complete = useCallback((answers) => {
    const next = { userId: newUserId(), ...answers }
    saveProfile(next)
    setProfile(next)
  }, [])

  // Forgets this browser's user: the next onboarding creates a new anonymous id.
  const logout = useCallback(() => {
    clearProfile()
    clearToday()
    setProfile(null)
  }, [])

  return { profile, complete, logout }
}
