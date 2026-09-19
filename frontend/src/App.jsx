import { useState } from 'react'
import AppShell from './components/AppShell.jsx'
import HomeScreen from './screens/HomeScreen.jsx'
import OnboardingScreen from './screens/OnboardingScreen.jsx'
import PlaceholderScreen from './screens/PlaceholderScreen.jsx'
import ProfileScreen from './screens/ProfileScreen.jsx'
import WelcomeScreen from './screens/WelcomeScreen.jsx'
import useProfile from './hooks/useProfile.js'

// Container: owns navigation. Screens and components only render and report events.
export default function App() {
  const { profile, complete, logout } = useProfile()
  const [screen, setScreen] = useState('home')
  const [started, setStarted] = useState(false)

  // No profile yet: welcome screen, then onboarding.
  if (!profile) {
    return started ? (
      <OnboardingScreen onComplete={complete} onExit={() => setStarted(false)} />
    ) : (
      <WelcomeScreen onStart={() => setStarted(true)} />
    )
  }

  const handleLogout = () => {
    logout()
    setScreen('home')
    setStarted(false)
  }

  // The challenge screen is the next step. Until then, starting a challenge does nothing.
  const startChallenge = () => {}

  return (
    <AppShell active={screen} onNavigate={setScreen}>
      {screen === 'home' && <HomeScreen profile={profile} onStartChallenge={startChallenge} />}
      {screen === 'progress' && <PlaceholderScreen title="Your progress" />}
      {screen === 'profile' && <ProfileScreen profile={profile} onLogout={handleLogout} />}
    </AppShell>
  )
}
