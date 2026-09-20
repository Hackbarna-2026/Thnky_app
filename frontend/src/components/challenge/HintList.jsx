import styles from './HintList.module.css'

const LABELS = ['NUDGE', 'CONCEPT', 'APPROACH']

// The hints revealed so far, from the gentlest to the strongest.
export default function HintList({ hints, revealed }) {
  return (
    <div className={styles.hints}>
      {hints.slice(0, revealed).map((hint, i) => (
        <div className={styles.hint} key={i}>
          <div className={styles.label}>
            {i + 1} / {hints.length} · {LABELS[i]}
          </div>
          <p className={styles.text}>{hint}</p>
        </div>
      ))}
    </div>
  )
}
