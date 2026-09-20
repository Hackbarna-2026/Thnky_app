import ToggleChip from '../ToggleChip.jsx'
import StepHeading from './StepHeading.jsx'
import { LANGUAGES } from '../../constants/onboarding.js'
import styles from './Step.module.css'

export default function LanguagesStep({ selected, onToggle }) {
  return (
    <>
      <StepHeading title="Which languages do you know?" subtitle="Pick as many as you like." />
      <div className={styles.chips}>
        {LANGUAGES.map((language) => (
          <ToggleChip
            key={language.key}
            selected={selected.includes(language.key)}
            onClick={() => onToggle(language.key)}
          >
            {language.name}
          </ToggleChip>
        ))}
      </div>
    </>
  )
}
