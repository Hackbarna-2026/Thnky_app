import Icon from '../Icon.jsx'
import styles from './ActivityLog.module.css'

export default function ActivityLog({ log }) {
  return (
    <div className={styles.card}>
      <div className={styles.title}>Recent activity</div>
      {log.length === 0 ? (
        <p className={styles.empty}>Nothing yet. Today's challenge is waiting.</p>
      ) : (
        <ul className={styles.list}>
          {log.map((entry, i) => (
            <li className={styles.item} key={i}>
              <span className={styles.name}>
                <Icon name={entry.correct ? 'check' : 'dash'} className={styles.icon} />
                <span className={styles.text}>{entry.title}</span>
              </span>
              <span className={styles.xp}>+{entry.xp}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
