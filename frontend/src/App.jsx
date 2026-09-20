import { useState } from 'react'
import AppShell from './components/AppShell.jsx'
import ChallengeFlow from './screens/ChallengeFlow.jsx'
import HomeScreen from './screens/HomeScreen.jsx'
import OnboardingScreen from './screens/OnboardingScreen.jsx'
import ProfileScreen from './screens/ProfileScreen.jsx'
import ProgressScreen from './screens/ProgressScreen.jsx'
import WelcomeScreen from './screens/WelcomeScreen.jsx'
import useChallengeSession from './hooks/useChallengeSession.js'
import useProfile from './hooks/useProfile.js'
import useProgress from './hooks/useProgress.js'
import { levelOf } from './lib/progress.js'
import { todayKey } from './lib/today.js'
import { dropToday, markTodayDone } from './storage/today.js'

const EXPIRED_NOTICE = 'That challenge is no longer available, so here is a new one.'

// Container: owns navigation. Screens and components only render and report events.
export default function App() {
  const { profile, complete, logout } = useProfile()
  const progress = useProgress()
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
    progress.reset()
    setScreen('home')
    setStarted(false)
  }

  // A graded challenge: apply its XP, mark today's challenge done if it was that one, show the verdict.
  const handleSubmitted = (results) => {
    const levelBefore = progress.level
    const next = progress.record(session.challenge, results.verdict)
    markTodayDone(todayKey(), session.challenge.id, results.verdict.xp)
    showResults({ ...results, leveledUp: levelOf(next.xp) > levelBefore })
  }

  // The backend lost the challenge: forget it and load a fresh one of the same skill.
  const handleExpired = () => {
    dropToday(session.challenge.id)
    start(session.challenge.skill, EXPIRED_NOTICE)
  }

  return (
    <AppShell active={screen} onNavigate={navigate}>
      {session ? (
        <ChallengeFlow
          session={session}
          userId={profile.userId}
          progress={progress}
          onSubmitted={handleSubmitted}
          onExpired={handleExpired}
          onNext={() => start(session.challenge.skill)}
          onRetry={() => start(session.skillKey)}
          onClose={close}
        />
      ) : (
        <>
          {screen === 'home' && (
            <HomeScreen
              profile={profile}
              progress={progress}
              onStartToday={openChallenge}
              onStartSkill={start}
            />
          )}
          {screen === 'progress' && <ProgressScreen progress={progress} skills={profile.skills} />}
          {screen === 'profile' && <ProfileScreen profile={profile} onLogout={handleLogout} />}
        </>
      )}
    </AppShell>
  )
}
