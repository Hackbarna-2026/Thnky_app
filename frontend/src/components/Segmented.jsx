import styles from './Segmented.module.css'

// Single choice out of a few. options: [{ key, label }]
export default function Segmented({ options, value, onChange, label }) {
  return (
    <div className={styles.group} role="radiogroup" aria-label={label}>
      {options.map((option) => (
        <button
          key={option.key}
          type="button"
          role="radio"
          aria-checked={option.key === value}
          className={`${styles.option} ${option.key === value ? styles.on : ''}`}
          onClick={() => onChange(option.key)}
        >
          {option.label}
        </button>
      ))}
    </div>
  )
}
