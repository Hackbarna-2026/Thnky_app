import styles from './ToggleChip.module.css'

export default function ToggleChip({ selected, onClick, children }) {
  return (
    <button
      type="button"
      className={`${styles.chip} ${selected ? styles.on : ''}`}
      aria-pressed={selected}
      onClick={onClick}
    >
      {children}
    </button>
  )
}
