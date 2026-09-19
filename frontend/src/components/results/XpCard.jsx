import styles from './XpCard.module.css'

const formatTime = (s) => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`

export default function XpCard({ xp, seconds, hintsUsed }) {
  return (
    <div className={styles.card}>
      <div className={styles.eyebrow}>XP earned</div>
      <div className={styles.xp}>+{xp}</div>
      <div className={styles.stats}>
        <div className={styles.stat}>
          <div className={styles.value}>{formatTime(seconds)}</div>
          <div className={styles.label}>Time</div>
        </div>
        <div className={styles.stat}>
          <div className={styles.value}>{hintsUsed}</div>
          <div className={styles.label}>Hints</div>
        </div>
      </div>
    </div>
  )
}
