import Icon from '../Icon.jsx'
import { SKILLS, SURPRISE } from '../../constants/skills.js'
import styles from './SkillPicker.module.css'

const OPTIONS = [...Object.entries(SKILLS), ['surprise', SURPRISE]]

export default function SkillPicker({ onPick }) {
  return (
    <section className={styles.section}>
      <div className={styles.eyebrow}>Or pick what you want to stretch</div>
      <div className={styles.grid}>
        {OPTIONS.map(([key, skill]) => (
          <button key={key} className={styles.skill} data-k={key} onClick={() => onPick(key)}>
            <Icon name={skill.icon} className={styles.icon} />
            <div className={styles.name}>{skill.name}</div>
            <div className={styles.sub}>{skill.tagline}</div>
          </button>
        ))}
      </div>
    </section>
  )
}
