import Icon from '../Icon.jsx'
import styles from './PrincipleNote.module.css'

export default function PrincipleNote() {
  return (
    <aside className={styles.card}>
      <Icon name="lock" className={styles.icon} />
      <p className={styles.text}>
        Thnky never hands you the answer. Ask for help and you get a nudge, then a concept, then an
        approach. The last step is always yours.
      </p>
    </aside>
  )
}
