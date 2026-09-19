import { useEffect, useRef, useState } from 'react'

// Seconds since the component mounted, ticking every second. read() gives the exact value on demand.
export default function useElapsedSeconds() {
  const startedAt = useRef(Date.now())
  const read = () => Math.floor((Date.now() - startedAt.current) / 1000)
  const [seconds, setSeconds] = useState(0)

  useEffect(() => {
    const id = setInterval(() => setSeconds(read()), 1000)
    return () => clearInterval(id)
  }, [])

  return { seconds, read }
}
