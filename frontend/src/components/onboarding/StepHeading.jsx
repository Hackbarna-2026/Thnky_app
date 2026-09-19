import styles from './StepHeading.module.css'

export default function StepHeading({ title, subtitle }) {
  return (
    <div className={styles.heading}>
      <h2 className={styles.title}>{title}</h2>
      {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
    </div>
  )
}
