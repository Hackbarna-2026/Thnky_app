import Segmented from '../Segmented.jsx'
import StepHeading from './StepHeading.jsx'
import { DAILY_MINUTES } from '../../constants/onboarding.js'

const OPTIONS = DAILY_MINUTES.map((minutes) => ({ key: minutes, label: `${minutes} min` }))

export default function TimeStep({ minutes, onChange }) {
  return (
    <>
      <StepHeading title="How much time do you want to spend each day?" />
      <Segmented label="Daily time" options={OPTIONS} value={minutes} onChange={onChange} />
    </>
  )
}
