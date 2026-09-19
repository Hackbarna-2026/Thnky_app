import styles from './CodeAnswer.module.css'

const EXTENSIONS = { js: 'js', py: 'py', sql: 'sql' }

// An editor for the learner's own code. The code is graded, never run.
export default function CodeAnswer({ challenge, value, onChange }) {
  const file = `solution.${EXTENSIONS[challenge.lang] ?? 'txt'}`

  return (
    <div className={styles.editor}>
      <div className={styles.bar}>
        <b />
        <b />
        <b />
        <span className={styles.file}>{file}</span>
      </div>
      <textarea
        className={styles.textarea}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        spellCheck={false}
        aria-label="Your code"
      />
    </div>
  )
}
