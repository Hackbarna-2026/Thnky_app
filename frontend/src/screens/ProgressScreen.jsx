import ActivityLog from '../components/progress/ActivityLog.jsx'
import BigStats from '../components/progress/BigStats.jsx'
import SkillBreakdown from '../components/progress/SkillBreakdown.jsx'

export default function ProgressScreen({ progress, skills }) {
  return (
    <>
      <h2 style={{ fontSize: 28 }}>Your progress</h2>
      <p style={{ color: 'var(--muted)', fontSize: 12.5, fontWeight: 600, marginTop: 4 }}>
        How much thinking you've done in Thnky. Nothing else.
      </p>
      <BigStats streak={progress.streak} xp={progress.xp} done={progress.done} level={progress.level} />
      <SkillBreakdown skills={skills} bySkill={progress.bySkill} />
      <ActivityLog log={progress.log} />
    </>
  )
}
