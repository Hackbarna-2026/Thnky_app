import Button from '../components/Button.jsx'
import Frame from '../components/Frame.jsx'
import Icon from '../components/Icon.jsx'
import Wordmark from '../components/Wordmark.jsx'
import SkillTiles from '../components/welcome/SkillTiles.jsx'
import styles from './WelcomeScreen.module.css'

export default function WelcomeScreen({ onStart }) {
  return (
    <Frame>
      <div className={styles.welcome}>
        <div className={styles.center}>
          <SkillTiles />
          <div className={styles.brand}>
            <Wordmark size={64} />
            <p className={styles.tagline}>A little thinking every day.</p>
          </div>
          <p className={styles.note}>
            <Icon name="lock" />
            Thnky trains you. It never solves it for you.
          </p>
        </div>

        <div className={styles.footer}>
          <Button onClick={onStart}>
            Get started <Icon name="arrow" />
          </Button>
        </div>
      </div>
    </Frame>
  )
}
