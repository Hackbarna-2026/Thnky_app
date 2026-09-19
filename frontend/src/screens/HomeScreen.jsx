import HomeHeader from '../components/home/HomeHeader.jsx'
import TodayHero from '../components/home/TodayHero.jsx'
import SkillPicker from '../components/home/SkillPicker.jsx'
import PrincipleNote from '../components/home/PrincipleNote.jsx'
import useProgress from '../hooks/useProgress.js'
import useTodayChallenge from '../hooks/useTodayChallenge.js'

export default function HomeScreen({ profile, onStartChallenge }) {
  const { streak, level } = useProgress()
  const today = useTodayChallenge()

  return (
    <>
      <HomeHeader streak={streak} level={level} />
      <TodayHero
        status={today.status}
        challenge={today.challenge}
        minutes={profile.minutes}
        onStart={() => onStartChallenge(today.challenge?.skill)}
        onRetry={today.retry}
      />
      <SkillPicker onPick={onStartChallenge} />
      <PrincipleNote />
    </>
  )
}
