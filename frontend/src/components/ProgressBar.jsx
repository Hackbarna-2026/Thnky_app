import { useEffect, useState } from 'react'
import styles from './ProgressBar.module.css'

// value is 0-100. It animates from empty on mount.
export default function ProgressBar({ value }) {
  const [shown, setShown] = useState(0)

  useEffect(() => {
    const id = setTimeout(() => setShown(value), 140)
    return () => clearTimeout(id)
  }, [value])

  return (
    <div className={styles.bar} role="progressbar" aria-valuemin={0} aria-valuemax={100} aria-valuenow={value}>
      <i className={styles.fill} style={{ width: `${shown}%` }} />
    </div>
  )
}
