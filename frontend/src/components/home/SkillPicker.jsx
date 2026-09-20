import Icon from '../Icon.jsx'
import { SKILLS, SURPRISE } from '../../constants/skills.js'
import styles from './SkillPicker.module.css'

export default function SkillPicker({ skills, onPick }) {
  const options = skills.map((key) => [key, SKILLS[key]])
  if (skills.length > 1) options.push(['surprise', SURPRISE])

  return (
    <section className={styles.section}>
      <div className={styles.eyebrow}>Or pick what you want to stretch</div>
      <div className={styles.grid}>
        {options.map(([key, skill]) => (
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
