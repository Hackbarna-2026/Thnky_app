import { useState } from 'react'
import { submitAnswer } from '../api/client.js'
import Button from '../components/Button.jsx'
import Icon from '../components/Icon.jsx'
import ChallengeCard from '../components/challenge/ChallengeCard.jsx'
import ChallengeTopBar from '../components/challenge/ChallengeTopBar.jsx'
import HintList from '../components/challenge/HintList.jsx'
import AnswerInput from '../components/challenge/answers/AnswerInput.jsx'
import { countWords } from '../components/challenge/answers/TextAnswer.jsx'
import useElapsedSeconds from '../hooks/useElapsedSeconds.js'
import styles from './ChallengeScreen.module.css'

// Index for choice and lines (null until chosen), text for code and text.
const initialAnswer = (challenge) => {
  if (challenge.type === 'code') return challenge.starter
  return challenge.type === 'text' ? '' : null
}

const squash = (text) => text.replace(/\s/g, '')

// A short reason the answer cannot be sent yet, or null when it is ready.
function problemWith(challenge, answer) {
  switch (challenge.type) {
    case 'choice':
      return answer === null ? 'Pick an option first' : null
    case 'lines':
      return answer === null ? 'Tap a line first' : null
    case 'code':
      return squash(answer) === squash(challenge.starter) ? 'Write something first' : null
    default:
      return countWords(answer) < 5 ? 'A few more words' : null
  }
}

// Container: owns the answer, the hints and the timer, and sends the answer for grading.
export default function ChallengeScreen({ challenge, notice, userId, onExit, onSubmitted, onExpired }) {
  const [answer, setAnswer] = useState(() => initialAnswer(challenge))
  const [hintsUsed, setHintsUsed] = useState(0)
  const [message, setMessage] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const { seconds, read } = useElapsedSeconds()

  const totalHints = challenge.hints.length
  const hintsLeft = totalHints - hintsUsed

  const change = (value) => {
    setAnswer(value)
    setMessage(null)
  }

  const submit = async () => {
    const problem = problemWith(challenge, answer)
    if (problem) return setMessage(problem)

    setSubmitting(true)
    setMessage(null)
    const spent = read()
    try {
      const verdict = await submitAnswer({
        challengeId: challenge.id,
        answer,
        hintsUsed,
        seconds: spent,
        userId,
      })
      onSubmitted({ verdict, seconds: spent, hintsUsed })
    } catch (error) {
      // The backend no longer has this challenge: retrying would never work, so get a new one.
      if (error.status === 404) return onExpired()
      setMessage("Couldn't send your answer. Try again.")
      setSubmitting(false)
    }
  }

  return (
    <>
      <ChallengeTopBar seconds={seconds} onBack={onExit} />

      {notice && (
        <p className={styles.notice} role="status">
          {notice}
        </p>
      )}

      <ChallengeCard challenge={challenge}>
        <AnswerInput challenge={challenge} value={answer} onChange={change} />
      </ChallengeCard>

      <HintList hints={challenge.hints} revealed={hintsUsed} />

      <div className={styles.actions}>
        {message && (
          <p className={styles.message} role="alert">
            {message}
          </p>
        )}
        <Button onClick={submit} disabled={submitting}>
          {submitting ? 'Checking…' : 'Submit answer'}
        </Button>
        <Button variant="secondary" onClick={() => setHintsUsed(hintsUsed + 1)} disabled={hintsLeft === 0}>
          {hintsLeft === 0 ? (
            <>
              <Icon name="lock" />
              No more hints
            </>
          ) : (
            <>
              <Icon name="bulb" />
              {hintsUsed === 0 ? 'Need a hint?' : `Another hint (${hintsLeft} left)`}
            </>
          )}
        </Button>
      </div>

      <p className={styles.note}>
        {hintsLeft === 0 ? (
          <>
            <Icon name="lock" />
            That is as far as Thnky goes. The last step is yours.
          </>
        ) : (
          `${totalHints} hints. Each one costs a little XP.`
        )}
      </p>
    </>
  )
}
