import Icon from './Icon.jsx'
import styles from './SelectCard.module.css'

// Multi-select card: icon, title, subtitle and a check when selected.
export default function SelectCard({ icon, tone = 'accent', title, subtitle, selected, onClick }) {
  return (
    <button
      type="button"
      className={`${styles.card} ${selected ? styles.selected : ''}`}
      data-tone={tone}
      aria-pressed={selected}
      onClick={onClick}
    >
      <Icon name={icon} className={styles.icon} />
      <span className={styles.text}>
        <span className={styles.title}>{title}</span>
        {subtitle && <span className={styles.subtitle}>{subtitle}</span>}
      </span>
      <span className={styles.check}>{selected && <Icon name="check" />}</span>
    </button>
  )
}
