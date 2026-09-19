import Icon from '../Icon.jsx'
import styles from './ChallengeStatus.module.css'

export default function ChallengeLoading() {
  return (
    <div className={styles.status} role="status">
      <Icon name="bolt" className={`${styles.icon} ${styles.pulse}`} />
      <h2 className={styles.title}>Preparing your challenge…</h2>
      <p className={styles.text}>This can take a few seconds.</p>
    </div>
  )
}
