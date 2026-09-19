import { useState } from 'react'
import Frame from '../components/Frame.jsx'
import OnboardingLayout from '../components/onboarding/OnboardingLayout.jsx'
import NameStep from '../components/onboarding/NameStep.jsx'
import IntroStep from '../components/onboarding/IntroStep.jsx'
import PracticeStep from '../components/onboarding/PracticeStep.jsx'
import LanguagesStep from '../components/onboarding/LanguagesStep.jsx'
import LevelStep from '../components/onboarding/LevelStep.jsx'
import TimeStep from '../components/onboarding/TimeStep.jsx'
import ReadyStep from '../components/onboarding/ReadyStep.jsx'
import { DEFAULT_LEVEL, DEFAULT_MINUTES, INTRO_SCREENS } from '../constants/onboarding.js'
import { SKILLS } from '../constants/skills.js'

const toggle = (list, item) => (list.includes(item) ? list.filter((x) => x !== item) : [...list, item])

// Container: owns the answers and the step order. The steps only render and report changes.
export default function OnboardingScreen({ onComplete, onExit }) {
  const [index, setIndex] = useState(0)
  const [name, setName] = useState('')
  const [skills, setSkills] = useState([])
  const [langs, setLangs] = useState([])
  const [levels, setLevels] = useState({})
  const [minutes, setMinutes] = useState(DEFAULT_MINUTES)

  const picksCode = skills.includes('code')
  // Keep skills in the order the app lists them, whatever order they were tapped in.
  const orderedSkills = Object.keys(SKILLS).filter((key) => skills.includes(key))

  const steps = [
    { key: 'name', cta: 'Continue', valid: name.trim().length > 0 },
    ...INTRO_SCREENS.map((_, i) => ({ key: `intro-${i}`, cta: 'Next', valid: true })),
    { key: 'practice', cta: 'Continue', valid: skills.length > 0 },
    ...(picksCode ? [{ key: 'languages', cta: 'Continue', valid: langs.length > 0 }] : []),
    { key: 'levels', cta: 'Continue', valid: true },
    { key: 'time', cta: 'Continue', valid: true },
    { key: 'ready', cta: "Let's think.", valid: true },
  ]
  const step = steps[index]

  const finish = () =>
    onComplete({
      name: name.trim(),
      skills: orderedSkills,
      langs: picksCode ? langs : [],
      levels: Object.fromEntries(orderedSkills.map((key) => [key, levels[key] ?? DEFAULT_LEVEL])),
      minutes,
    })

  const next = () => {
    if (!step.valid) return
    if (index === steps.length - 1) finish()
    else setIndex(index + 1)
  }

  const renderStep = () => {
    if (step.key === 'name') return <NameStep name={name} onChange={setName} onSubmit={next} />
    if (step.key.startsWith('intro-')) return <IntroStep {...INTRO_SCREENS[Number(step.key.slice(6))]} />
    if (step.key === 'practice')
      return <PracticeStep selected={skills} onToggle={(key) => setSkills((s) => toggle(s, key))} />
    if (step.key === 'languages')
      return <LanguagesStep selected={langs} onToggle={(key) => setLangs((l) => toggle(l, key))} />
    if (step.key === 'levels')
      return (
        <LevelStep
          skills={orderedSkills}
          levels={levels}
          onChange={(key, level) => setLevels((l) => ({ ...l, [key]: level }))}
        />
      )
    if (step.key === 'time') return <TimeStep minutes={minutes} onChange={setMinutes} />
    return <ReadyStep name={name.trim()} />
  }

  return (
    <Frame>
      <OnboardingLayout
        step={index}
        total={steps.length}
        onBack={() => (index === 0 ? onExit() : setIndex(index - 1))}
        cta={step.cta}
        canContinue={step.valid}
        onContinue={next}
      >
        {renderStep()}
      </OnboardingLayout>
    </Frame>
  )
}
