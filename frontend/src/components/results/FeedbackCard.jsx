import Icon from '../Icon.jsx'
import styles from './FeedbackCard.module.css'

// What the learner did well and what to work on. Neither repeats the solution.
export default function FeedbackCard({ good, improve }) {
  return (
    <div className={styles.card}>
      <div className={styles.item}>
        <Icon name="check" className={styles.mark} />
        <div>
          <strong className={styles.heading}>What you did well</strong>
          <p className={styles.text}>{good}</p>
        </div>
      </div>
      <div className={styles.item}>
        <Icon name="wrench" className={`${styles.mark} ${styles.warn}`} />
        <div>
          <strong className={styles.heading}>What to work on</strong>
          <p className={styles.text}>{improve}</p>
        </div>
      </div>
    </div>
  )
}
