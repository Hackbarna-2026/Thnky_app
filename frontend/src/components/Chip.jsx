import styles from './Chip.module.css'

export default function Chip({ hot = false, children }) {
  return <span className={`${styles.chip} ${hot ? styles.hot : ''}`}>{children}</span>
}
