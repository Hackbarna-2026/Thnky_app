import styles from './Button.module.css'

export default function Button({ variant = 'primary', children, ...props }) {
  return (
    <button className={`${styles.btn} ${variant === 'secondary' ? styles.secondary : ''}`} {...props}>
      {children}
    </button>
  )
}
