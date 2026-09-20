import ProgressBar from '../ProgressBar.jsx'
import { XP_PER_LEVEL, xpIntoLevel, xpToNextLevel } from '../../lib/progress.js'
import styles from './LevelCard.module.css'

export default function LevelCard({ level, xp, leveledUp }) {
  return (
    <div className={styles.card}>
      <div className={styles.row}>
        <strong>{leveledUp ? `Level ${level} unlocked` : `Level ${level}`}</strong>
        <span className={styles.togo}>
          {xpToNextLevel(xp)} XP to Level {level + 1}
        </span>
      </div>
      <ProgressBar value={(xpIntoLevel(xp) / XP_PER_LEVEL) * 100} />
    </div>
  )
}
