// Stand-in for GET /api/challenges/next while the backend endpoint is not ready.
// Only used with `npm run dev:mock`. Same shape as the challenge schema in CLAUDE.md §8.
export const MOCK_CHALLENGE = {
  id: 'switches',
  skill: 'logic',
  diff: 'Easy',
  type: 'choice',
  hook: 'Three bulbs. One trip upstairs.',
  title: 'The three switches',
  desc: 'Three switches down here control three bulbs up there. Flip them as much as you like — but you only get to walk up once.',
  options: [
    'Flip one, go up, come back down, flip the next',
    'Switch 1 on for ten minutes, off, switch 2 on, then go up',
    'Turn all three on and see which lights first',
    'Cannot be done in one trip',
  ],
  answer: 1,
  hints: [
    'One trip has to give you three separate facts.',
    'A bulb has more than one state you can read. Light is only the obvious one.',
    'Leave one on long enough to get hot, then switch it off before you climb.',
  ],
  good: 'You looked for a second signal instead of accepting the constraint as given.',
  improve: 'Name the rule out loud first: one trip means three distinguishable states, not two.',
  insight: 'Most impossible constraints are only impossible if you accept the variables you were handed.',
}
