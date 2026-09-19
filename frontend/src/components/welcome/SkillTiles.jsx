import Icon from '../Icon.jsx'
import { SKILLS } from '../../constants/skills.js'
import styles from './SkillTiles.module.css'

const TONES = { code: 'accent', logic: 'hot', critical: 'cool' }

// The three skills as tiles: the whole product at a glance.
export default function SkillTiles() {
  return (
    <div className={styles.tiles} aria-hidden="true">
      {Object.entries(SKILLS).map(([key, skill]) => (
        <div key={key} className={styles.tile} data-tone={TONES[key]}>
          <Icon name={skill.icon} />
        </div>
      ))}
    </div>
  )
}
