import styles from './Frame.module.css'

// The phone-sized frame every screen lives in.
export default function Frame({ children }) {
  return <div className={styles.frame}>{children}</div>
}
