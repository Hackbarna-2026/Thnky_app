import Chip from '../Chip.jsx'
import Icon from '../Icon.jsx'
import Wordmark from '../Wordmark.jsx'
import styles from './HomeHeader.module.css'

export default function HomeHeader({ streak, level }) {
  return (
    <header className={styles.header}>
      <div>
        <Wordmark />
        <div className={styles.tagline}>A little thinking every day.</div>
      </div>
      <div className={styles.chips}>
        <Chip hot>
          <Icon name="flame" />
          {streak}
        </Chip>
        <Chip>Lv {level}</Chip>
      </div>
    </header>
  )
}
