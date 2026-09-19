import Button from '../Button.jsx'
import Icon from '../Icon.jsx'
import styles from './OnboardingLayout.module.css'

// Progress bar and back button on top, step content in the middle, one CTA at the bottom.
export default function OnboardingLayout({ step, total, onBack, cta, canContinue, onContinue, children }) {
  return (
    <div className={styles.layout}>
      <div className={styles.top}>
        <button className={styles.back} onClick={onBack} aria-label="Back">
          <Icon name="back" />
        </button>
        <div className={styles.progress} role="progressbar" aria-valuemin={1} aria-valuemax={total} aria-valuenow={step + 1}>
          <i style={{ width: `${((step + 1) / total) * 100}%` }} />
        </div>
      </div>

      <div className={styles.content} key={step}>
        {children}
      </div>

      <div className={styles.footer}>
        <Button onClick={onContinue} disabled={!canContinue}>
          {cta}
        </Button>
      </div>
    </div>
  )
}
