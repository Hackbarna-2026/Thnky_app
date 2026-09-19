import Icon from '../Icon.jsx'
import styles from './ReadyStep.module.css'

export default function ReadyStep({ name }) {
  return (
    <div className={styles.ready}>
      <div className={styles.badge}>
        <Icon name="bolt" />
      </div>
      <p className={styles.greeting}>All set, {name}.</p>
      <h2 className={styles.text}>Ready to keep your brain switched on?</h2>
    </div>
  )
}
