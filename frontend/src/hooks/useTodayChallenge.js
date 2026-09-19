import { useCallback, useEffect, useState } from 'react'
import { getNextChallenge } from '../api/client.js'

// status: 'loading' | 'ready' | 'error'
export default function useTodayChallenge() {
  const [state, setState] = useState({ status: 'loading', challenge: null })
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    let cancelled = false
    setState({ status: 'loading', challenge: null })
    getNextChallenge()
      .then((challenge) => !cancelled && setState({ status: 'ready', challenge }))
      .catch(() => !cancelled && setState({ status: 'error', challenge: null }))
    return () => {
      cancelled = true
    }
  }, [attempt])

  const retry = useCallback(() => setAttempt((n) => n + 1), [])

  return { ...state, retry }
}
