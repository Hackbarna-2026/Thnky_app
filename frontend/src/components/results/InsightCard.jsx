import Icon from '../Icon.jsx'
import styles from './InsightCard.module.css'

export default function InsightCard({ insight }) {
  return (
    <div className={styles.card}>
      <div className={styles.label}>
        <Icon name="spark" />
        Think about this
      </div>
      <p className={styles.text}>{insight}</p>
    </div>
  )
}
