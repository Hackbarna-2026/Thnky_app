import HomeHeader from '../components/home/HomeHeader.jsx'
import TodayHero from '../components/home/TodayHero.jsx'
import SkillPicker from '../components/home/SkillPicker.jsx'
import PrincipleNote from '../components/home/PrincipleNote.jsx'
import useTodayChallenge from '../hooks/useTodayChallenge.js'

export default function HomeScreen({ profile, progress, onStartToday, onStartSkill }) {
  const today = useTodayChallenge(profile)

  return (
    <>
      <HomeHeader streak={progress.streak} level={progress.level} />
      <TodayHero
        status={today.status}
        challenge={today.challenge}
        done={today.done}
        xp={today.xp}
        minutes={profile.minutes}
        onStart={() => onStartToday(today.challenge)}
        onPracticeMore={onStartSkill}
        onRetry={today.retry}
      />
      <SkillPicker skills={profile.skills} onPick={onStartSkill} />
      <PrincipleNote />
    </>
  )
}
