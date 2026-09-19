import styles from './TextAnswer.module.css'

export const countWords = (text) => text.trim().split(/\s+/).filter(Boolean).length

export default function TextAnswer({ value, onChange }) {
  const words = countWords(value)

  return (
    <>
      <textarea
        className={styles.textarea}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Take your time. Two or three sentences."
        aria-label="Your answer"
      />
      <div className={styles.counter}>
        {words} word{words === 1 ? '' : 's'}
      </div>
    </>
  )
}
