import Icon from '../Icon.jsx'
import styles from './ChallengeTopBar.module.css'

const WARN_AFTER_SECONDS = 300

const format = (s) => `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`

export default function ChallengeTopBar({ seconds, onBack }) {
  return (
    <div className={styles.bar}>
      <button className={styles.back} onClick={onBack} aria-label="Back">
        <Icon name="back" />
      </button>
      <span className={`${styles.timer} ${seconds > WARN_AFTER_SECONDS ? styles.warn : ''}`}>
        <Icon name="clock" />
        {format(seconds)}
      </span>
    </div>
  )
}
