import Icon from '../Icon.jsx'
import styles from './BigStats.module.css'

export default function BigStats({ streak, xp, done, level }) {
  return (
    <div className={styles.grid}>
      <div className={`${styles.big} ${styles.hot}`}>
        <div className={styles.value}>
          <Icon name="flame" />
          {streak}
        </div>
        <div className={styles.label}>Day streak</div>
      </div>
      <div className={styles.big}>
        <div className={styles.value}>{xp}</div>
        <div className={styles.label}>Total XP</div>
      </div>
      <div className={styles.big}>
        <div className={styles.value}>{done}</div>
        <div className={styles.label}>Challenges done</div>
      </div>
      <div className={styles.big}>
        <div className={styles.value}>{level}</div>
        <div className={styles.label}>Current level</div>
      </div>
    </div>
  )
}
