import Icon from '../Icon.jsx'
import ProgressBar from '../ProgressBar.jsx'
import { SKILLS } from '../../constants/skills.js'
import styles from './SkillBreakdown.module.css'

// Challenges done per skill, relative to the skill the learner did most.
export default function SkillBreakdown({ skills, bySkill }) {
  const max = Math.max(1, ...skills.map((key) => bySkill[key] ?? 0))

  return (
    <div className={styles.card}>
      <div className={styles.title}>By skill</div>
      {skills.map((key) => {
        const count = bySkill[key] ?? 0
        return (
          <div className={styles.row} key={key}>
            <div className={styles.head}>
              <span className={styles.name}>
                <Icon name={SKILLS[key].icon} />
                {SKILLS[key].name}
              </span>
              <span>{count}</span>
            </div>
            <ProgressBar value={Math.round((count / max) * 100)} />
          </div>
        )
      })}
    </div>
  )
}
