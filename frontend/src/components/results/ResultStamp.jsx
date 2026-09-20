import styles from './ResultStamp.module.css'

export default function ResultStamp({ correct }) {
  return (
    <div className={`${styles.stamp} ${correct ? '' : styles.miss}`}>
      {correct ? 'Nailed it' : 'Not quite'}
    </div>
  )
}
