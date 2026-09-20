import Button from '../components/Button.jsx'
import Icon from '../components/Icon.jsx'
import FeedbackCard from '../components/results/FeedbackCard.jsx'
import InsightCard from '../components/results/InsightCard.jsx'
import LevelCard from '../components/results/LevelCard.jsx'
import ResultStamp from '../components/results/ResultStamp.jsx'
import XpCard from '../components/results/XpCard.jsx'

export default function ResultsScreen({ verdict, seconds, hintsUsed, leveledUp, progress, onNext, onDone }) {
  return (
    <>
      <ResultStamp correct={verdict.correct} />
      <XpCard xp={verdict.xp} seconds={seconds} hintsUsed={hintsUsed} streak={progress.streak} />
      <FeedbackCard good={verdict.good} improve={verdict.improve} />
      <InsightCard insight={verdict.insight} />
      <LevelCard level={progress.level} xp={progress.xp} leveledUp={leveledUp} />

      <div style={{ marginTop: 18, display: 'grid', gap: 9 }}>
        <Button onClick={onNext}>
          Next challenge <Icon name="arrow" />
        </Button>
        <Button variant="secondary" onClick={onDone}>
          That's enough for today
        </Button>
      </div>
    </>
  )
}
