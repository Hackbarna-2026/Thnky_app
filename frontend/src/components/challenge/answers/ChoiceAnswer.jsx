import SvgFigure from '../SvgFigure.jsx'
import styles from './ChoiceAnswer.module.css'

const letter = (i) => String.fromCharCode(65 + i)

// Text options, or picture options when the challenge has optionsSvg.
export default function ChoiceAnswer({ challenge, value, onChange }) {
  if (challenge.optionsSvg) {
    return (
      <div className={`${styles.visual} ${challenge.optionsSvg.length > 4 ? styles.three : ''}`}>
        {challenge.optionsSvg.map((markup, i) => (
          <button
            key={i}
            type="button"
            className={`${styles.vopt} ${value === i ? styles.selected : ''}`}
            aria-pressed={value === i}
            aria-label={`Option ${letter(i)}`}
            onClick={() => onChange(i)}
          >
            <SvgFigure markup={markup} className={styles.svg} />
            <span className={styles.vkey}>{letter(i)}</span>
          </button>
        ))}
      </div>
    )
  }

  return (
    <div className={styles.options}>
      {challenge.options.map((option, i) => (
        <button
          key={i}
          type="button"
          className={`${styles.opt} ${value === i ? styles.selected : ''}`}
          aria-pressed={value === i}
          onClick={() => onChange(i)}
        >
          <span className={styles.key}>{letter(i)}</span>
          <span>{option}</span>
        </button>
      ))}
    </div>
  )
}
