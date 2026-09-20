import Icon from '../Icon.jsx'
import Segmented from '../Segmented.jsx'
import StepHeading from './StepHeading.jsx'
import { DEFAULT_LEVEL, LEVELS } from '../../constants/onboarding.js'
import { SKILLS } from '../../constants/skills.js'
import styles from './LevelStep.module.css'

// One row per chosen skill, in the same order as the practice step.
export default function LevelStep({ skills, levels, onChange }) {
  return (
    <>
      <StepHeading title="How would you describe your level?" subtitle="Be honest. We adapt from here." />
      <div className={styles.rows}>
        {skills.map((key) => (
          <div className={styles.row} key={key}>
            <div className={styles.skill}>
              <Icon name={SKILLS[key].icon} className={styles.icon} />
              {SKILLS[key].name}
            </div>
            <Segmented
              label={`${SKILLS[key].name} level`}
              options={LEVELS}
              value={levels[key] ?? DEFAULT_LEVEL}
              onChange={(level) => onChange(key, level)}
            />
          </div>
        ))}
      </div>
    </>
  )
}
