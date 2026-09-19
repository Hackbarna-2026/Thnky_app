import { useEffect, useRef } from 'react'
import Frame from './Frame.jsx'
import TabBar from './TabBar.jsx'
import styles from './AppShell.module.css'

export default function AppShell({ active, onNavigate, children }) {
  const scrollRef = useRef(null)

  useEffect(() => {
    scrollRef.current?.scrollTo(0, 0)
  }, [active])

  return (
    <Frame>
      <div className={styles.scroll} ref={scrollRef}>
        {children}
      </div>
      <TabBar active={active} onNavigate={onNavigate} />
    </Frame>
  )
}
