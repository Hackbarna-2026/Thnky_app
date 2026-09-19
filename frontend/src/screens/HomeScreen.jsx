import HomeHeader from '../components/home/HomeHeader.jsx'
import TodayHero from '../components/home/TodayHero.jsx'
import SkillPicker from '../components/home/SkillPicker.jsx'
import PrincipleNote from '../components/home/PrincipleNote.jsx'
import useProgress from '../hooks/useProgress.js'
import useTodayChallenge from '../hooks/useTodayChallenge.js'

export default function HomeScreen({ profile, onStartToday, onStartSkill }) {
  const { streak, level } = useProgress()
  const today = useTodayChallenge(profile)

  return (
    <>
      <HomeHeader streak={streak} level={level} />
      <TodayHero
        status={today.status}
        challenge={today.challenge}
        minutes={profile.minutes}
        onStart={() => onStartToday(today.challenge)}
        onRetry={today.retry}
      />
      <SkillPicker skills={profile.skills} onPick={onStartSkill} />
      <PrincipleNote />
    </>
  )
}
