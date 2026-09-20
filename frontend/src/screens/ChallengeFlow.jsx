import ChallengeError from '../components/challenge/ChallengeError.jsx'
import ChallengeLoading from '../components/challenge/ChallengeLoading.jsx'
import ChallengeScreen from './ChallengeScreen.jsx'
import ResultsScreen from './ResultsScreen.jsx'

// Shows the step of the challenge session the learner is on.
export default function ChallengeFlow({
  session,
  userId,
  progress,
  onSubmitted,
  onExpired,
  onNext,
  onRetry,
  onClose,
}) {
  switch (session.phase) {
    case 'loading':
      return <ChallengeLoading />
    case 'error':
      return <ChallengeError onRetry={onRetry} onBack={onClose} />
    case 'challenge':
      return (
        <ChallengeScreen
          key={session.challenge.id}
          challenge={session.challenge}
          notice={session.notice}
          userId={userId}
          onExit={onClose}
          onSubmitted={onSubmitted}
          onExpired={onExpired}
        />
      )
    default:
      return (
        <ResultsScreen
          verdict={session.verdict}
          seconds={session.seconds}
          hintsUsed={session.hintsUsed}
          leveledUp={session.leveledUp}
          progress={progress}
          onNext={onNext}
          onDone={onClose}
        />
      )
  }
}
