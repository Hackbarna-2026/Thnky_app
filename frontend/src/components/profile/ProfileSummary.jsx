import Chip from '../Chip.jsx'
import Icon from '../Icon.jsx'
import { LANGUAGES, LEVELS } from '../../constants/onboarding.js'
import { SKILLS } from '../../constants/skills.js'
import styles from './ProfileSummary.module.css'

const levelLabel = (key) => LEVELS.find((level) => level.key === key)?.label
const languageName = (key) => LANGUAGES.find((language) => language.key === key)?.name ?? key

// What the onboarding collected, as the app remembers it.
export default function ProfileSummary({ profile }) {
  return (
    <div className={styles.card}>
      <div className={styles.name}>{profile.name}</div>

      <div className={styles.group}>
        <div className={styles.label}>Practicing</div>
        <div className={styles.chips}>
          {profile.skills.map((key) => (
            <Chip key={key}>
              <Icon name={SKILLS[key].icon} />
              {SKILLS[key].name} · {levelLabel(profile.levels[key])}
            </Chip>
          ))}
        </div>
      </div>

      {profile.langs.length > 0 && (
        <div className={styles.group}>
          <div className={styles.label}>Languages</div>
          <div className={styles.chips}>
            {profile.langs.map((key) => (
              <Chip key={key}>{languageName(key)}</Chip>
            ))}
          </div>
        </div>
      )}

      <div className={styles.group}>
        <div className={styles.label}>Daily time</div>
        <Chip>
          <Icon name="clock" />
          {profile.minutes} min
        </Chip>
      </div>
    </div>
  )
}
