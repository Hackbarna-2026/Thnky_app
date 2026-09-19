import { useState } from 'react'
import { levelOf, loadProgress } from '../storage/progress.js'

export default function useProgress() {
  const [progress] = useState(loadProgress)
  return { ...progress, level: levelOf(progress.xp) }
}
