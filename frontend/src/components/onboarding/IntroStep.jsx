import Icon from '../Icon.jsx'
import styles from './IntroStep.module.css'

export default function IntroStep({ icon, tone, text }) {
  return (
    <div className={styles.intro}>
      <div className={styles.badge} data-tone={tone}>
        <Icon name={icon} />
      </div>
      <h2 className={styles.text}>{text}</h2>
    </div>
  )
}
