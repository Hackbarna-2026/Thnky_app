import StepHeading from './StepHeading.jsx'
import styles from './NameStep.module.css'

// The "login": just a name. No account, no password, nothing leaves the browser.
export default function NameStep({ name, onChange, onSubmit }) {
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault()
        onSubmit()
      }}
    >
      <StepHeading title="What should we call you?" subtitle="Just a first name is fine." />
      <input
        className={styles.input}
        type="text"
        value={name}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Your name"
        maxLength={30}
        autoComplete="off"
        autoFocus
        aria-label="Your name"
      />
    </form>
  )
}
