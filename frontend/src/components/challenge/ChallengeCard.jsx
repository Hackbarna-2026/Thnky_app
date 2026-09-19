import Icon from '../Icon.jsx'
import SvgFigure from './SvgFigure.jsx'
import { SKILLS } from '../../constants/skills.js'
import styles from './ChallengeCard.module.css'

// Coloured head with the hook, then the description, the optional figure and the answer area.
export default function ChallengeCard({ challenge, children }) {
  const skill = SKILLS[challenge.skill]

  return (
    <div className={styles.card} data-skill={challenge.skill}>
      <div className={styles.head}>
        <Icon name={skill.icon} className={styles.mark} />
        <div className={styles.tag}>
          <span>{skill.name.toUpperCase()}</span>
          <span>·</span>
          <span>{challenge.diff.toUpperCase()}</span>
        </div>
        <div className={styles.hook}>{challenge.hook}</div>
      </div>
      <div className={styles.body}>
        <p className={styles.desc}>{challenge.desc}</p>
        {challenge.figure && <SvgFigure markup={challenge.figure} className={styles.figure} />}
        {children}
      </div>
    </div>
  )
}
