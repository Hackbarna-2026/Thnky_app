import { useState } from 'react'
import AppShell from './components/AppShell.jsx'
import ChallengeFlow from './screens/ChallengeFlow.jsx'
import HomeScreen from './screens/HomeScreen.jsx'
import OnboardingScreen from './screens/OnboardingScreen.jsx'
import PlaceholderScreen from './screens/PlaceholderScreen.jsx'
import ProfileScreen from './screens/ProfileScreen.jsx'
import WelcomeScreen from './screens/WelcomeScreen.jsx'
import useChallengeSession from './hooks/useChallengeSession.js'
import useProfile from './hooks/useProfile.js'

// Container: owns navigation. Screens and components only render and report events.
export default function App() {
  const { profile, complete, logout } = useProfile()
  const { session, openChallenge, start, showResults, close } = useChallengeSession(profile)
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

  const navigate = (next) => {
    close()
    setScreen(next)
  }

  const handleLogout = () => {
    close()
    logout()
    setScreen('home')
    setStarted(false)
  }

  return (
    <AppShell active={screen} onNavigate={navigate}>
      {session ? (
        <ChallengeFlow
          session={session}
          userId={profile.userId}
          onSubmitted={showResults}
          onNext={() => start(session.challenge.skill)}
          onRetry={() => start(session.skillKey)}
          onClose={close}
        />
      ) : (
        <>
          {screen === 'home' && (
            <HomeScreen profile={profile} onStartToday={openChallenge} onStartSkill={start} />
          )}
          {screen === 'progress' && <PlaceholderScreen title="Your progress" />}
          {screen === 'profile' && <ProfileScreen profile={profile} onLogout={handleLogout} />}
        </>
      )}
    </AppShell>
  )
}
