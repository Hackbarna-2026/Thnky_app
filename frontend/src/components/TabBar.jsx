import Icon from './Icon.jsx'
import styles from './TabBar.module.css'

const TABS = [
  { key: 'home', label: 'Today', icon: 'bolt' },
  { key: 'progress', label: 'Progress', icon: 'chart' },
  { key: 'profile', label: 'Profile', icon: 'user' },
]

export default function TabBar({ active, onNavigate }) {
  return (
    <nav className={styles.tabs}>
      {TABS.map((tab) => (
        <button
          key={tab.key}
          className={`${styles.tab} ${tab.key === active ? styles.on : ''}`}
          onClick={() => onNavigate(tab.key)}
          aria-current={tab.key === active ? 'page' : undefined}
        >
          <Icon name={tab.icon} className={styles.icon} />
          <span className={styles.label}>{tab.label}</span>
        </button>
      ))}
    </nav>
  )
}
