import styles from './LinesAnswer.module.css'

// A code listing where the learner taps the line they think is wrong.
export default function LinesAnswer({ challenge, value, onChange }) {
  return (
    <>
      <div className={styles.lines}>
        <div className={styles.bar}>
          <b />
          <b />
          <b />
          <span className={styles.file}>{challenge.file}</span>
        </div>
        {challenge.lines.map((line, i) => (
          <button
            key={i}
            type="button"
            className={`${styles.line} ${value === i ? styles.selected : ''}`}
            aria-pressed={value === i}
            onClick={() => onChange(i)}
          >
            <span className={styles.no}>{i + 1}</span>
            <span className={styles.code}>{line || ' '}</span>
          </button>
        ))}
      </div>
      <p className={styles.note}>Tap the line you think is wrong.</p>
    </>
  )
}
