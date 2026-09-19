import Button from '../Button.jsx'
import Chip from '../Chip.jsx'
import Icon from '../Icon.jsx'
import { SKILLS } from '../../constants/skills.js'
import styles from './TodayHero.module.css'

export default function TodayHero({ status, challenge, minutes, onStart, onRetry }) {
  const skill = challenge ? SKILLS[challenge.skill] : null

  return (
    <section className={styles.hero}>
      <div className={styles.band}>
        <span className={styles.label}>
          <Icon name="bolt" />
          Today's workout
        </span>
        <span className={styles.diff}>{challenge?.diff}</span>
      </div>

      <div className={styles.body}>
        {skill && <Icon name={skill.icon} className={styles.mark} />}

        {status === 'loading' && <h2 className={styles.title}>Loading…</h2>}

        {status === 'error' && (
          <>
            <h2 className={styles.title}>Couldn't load today's challenge</h2>
            <p className={styles.hook}>Check your connection and try again.</p>
            <div className={styles.actions}>
              <Button onClick={onRetry}>Try again</Button>
            </div>
          </>
        )}

        {status === 'ready' && (
          <>
            <h2 className={styles.title}>{challenge.title}</h2>
            <p className={styles.hook}>{challenge.hook}</p>
            <div className={styles.meta}>
              <Chip>
                <Icon name={skill.icon} />
                {skill.name}
              </Chip>
              <Chip>
                <Icon name="clock" />
                {minutes} min
              </Chip>
            </div>
            <Button onClick={onStart}>
              Start today's challenge <Icon name="arrow" />
            </Button>
          </>
        )}
      </div>
    </section>
  )
}
