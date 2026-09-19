import Button from '../Button.jsx'
import Icon from '../Icon.jsx'
import styles from './ChallengeStatus.module.css'

export default function ChallengeError({ onRetry, onBack }) {
  return (
    <div className={styles.status} role="alert">
      <Icon name="wrench" className={styles.icon} />
      <h2 className={styles.title}>Couldn't load the challenge</h2>
      <p className={styles.text}>Check your connection and try again.</p>
      <div className={styles.actions}>
        <Button onClick={onRetry}>Try again</Button>
        <Button variant="secondary" onClick={onBack}>
          Back
        </Button>
      </div>
    </div>
  )
}
