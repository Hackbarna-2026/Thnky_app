import styles from './Wordmark.module.css'

export default function Wordmark({ size = 27 }) {
  return (
    <div className={styles.wordmark} style={{ fontSize: size }}>
      Thnky<em>.</em>
    </div>
  )
}
