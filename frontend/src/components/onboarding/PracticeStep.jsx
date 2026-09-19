import SelectCard from '../SelectCard.jsx'
import StepHeading from './StepHeading.jsx'
import { SKILLS } from '../../constants/skills.js'
import styles from './Step.module.css'

const TONES = { code: 'accent', logic: 'hot', critical: 'cool' }

export default function PracticeStep({ selected, onToggle }) {
  return (
    <>
      <StepHeading title="What do you want to practice?" subtitle="Pick as many as you like." />
      <div className={styles.stack}>
        {Object.entries(SKILLS).map(([key, skill]) => (
          <SelectCard
            key={key}
            icon={skill.icon}
            tone={TONES[key]}
            title={skill.name}
            subtitle={skill.tagline}
            selected={selected.includes(key)}
            onClick={() => onToggle(key)}
          />
        ))}
      </div>
    </>
  )
}
