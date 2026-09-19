import ChallengeError from '../components/challenge/ChallengeError.jsx'
import ChallengeLoading from '../components/challenge/ChallengeLoading.jsx'
import ChallengeScreen from './ChallengeScreen.jsx'
import ResultsScreen from './ResultsScreen.jsx'

// Shows the step of the challenge session the learner is on.
export default function ChallengeFlow({ session, userId, onSubmitted, onNext, onRetry, onClose }) {
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
          userId={userId}
          onExit={onClose}
          onSubmitted={onSubmitted}
        />
      )
    default:
      return (
        <ResultsScreen
          verdict={session.verdict}
          seconds={session.seconds}
          hintsUsed={session.hintsUsed}
          onNext={onNext}
          onDone={onClose}
        />
      )
  }
}
