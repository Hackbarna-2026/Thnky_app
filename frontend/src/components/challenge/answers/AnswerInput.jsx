import ChoiceAnswer from './ChoiceAnswer.jsx'
import CodeAnswer from './CodeAnswer.jsx'
import LinesAnswer from './LinesAnswer.jsx'
import TextAnswer from './TextAnswer.jsx'

// Picks the right answer control for the challenge type. Values are an index for choice and
// lines (null until chosen), and a string for code and text.
export default function AnswerInput({ challenge, value, onChange }) {
  switch (challenge.type) {
    case 'choice':
      return <ChoiceAnswer challenge={challenge} value={value} onChange={onChange} />
    case 'lines':
      return <LinesAnswer challenge={challenge} value={value} onChange={onChange} />
    case 'code':
      return <CodeAnswer challenge={challenge} value={value} onChange={onChange} />
    default:
      return <TextAnswer value={value} onChange={onChange} />
  }
}
