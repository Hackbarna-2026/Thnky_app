const ICONS = {
  code: (
    <>
      <polyline points="8 6 2.5 12 8 18" />
      <polyline points="16 6 21.5 12 16 18" />
      <line x1="13.5" y1="4" x2="10.5" y2="20" />
    </>
  ),
  puzzle: (
    <path d="M4 5.5A1.5 1.5 0 0 1 5.5 4H9a2.2 2.2 0 0 1 4.4 0H17a1.5 1.5 0 0 1 1.5 1.5V9a2.2 2.2 0 0 1 0 4.4v3.1A1.5 1.5 0 0 1 17 18h-3.6a2.2 2.2 0 0 0-4.4 0H5.5A1.5 1.5 0 0 1 4 16.5v-3.1a2.2 2.2 0 0 0 0-4.4z" />
  ),
  lens: (
    <>
      <circle cx="10.5" cy="10.5" r="6.5" />
      <line x1="20" y1="20" x2="15.2" y2="15.2" />
      <path d="M8 10.5a2.5 2.5 0 0 1 2.5-2.5" />
    </>
  ),
  dice: (
    <>
      <rect x="3.5" y="3.5" width="17" height="17" rx="4.5" />
      <circle cx="8.6" cy="8.6" r="1.1" fill="currentColor" stroke="none" />
      <circle cx="15.4" cy="15.4" r="1.1" fill="currentColor" stroke="none" />
      <circle cx="12" cy="12" r="1.1" fill="currentColor" stroke="none" />
    </>
  ),
  flame: (
    <path d="M12 2.5c3.2 3.8 6 5.6 6 9.4a6 6 0 0 1-12 0c0-2.2 1-3.9 2.6-5.4.3 1.5 1.1 2.4 2 2.7-.6-2.6.2-4.9 1.4-6.7z" />
  ),
  bolt: <path d="M13.5 2.5 5 13.5h5.5L9.5 21.5 19 10.5h-6z" />,
  chart: (
    <>
      <line x1="4" y1="20.5" x2="20.5" y2="20.5" />
      <line x1="7.5" y1="20.5" x2="7.5" y2="13" />
      <line x1="12" y1="20.5" x2="12" y2="6.5" />
      <line x1="16.5" y1="20.5" x2="16.5" y2="10" />
    </>
  ),
  user: (
    <>
      <circle cx="12" cy="8.5" r="4" />
      <path d="M4.5 20.5a7.5 7.5 0 0 1 15 0" />
    </>
  ),
  back: <polyline points="14.5 5 8 12 14.5 19" />,
  bulb: (
    <>
      <path d="M12 3a6 6 0 0 0-3.4 10.9c.4.3.6.8.6 1.3v.8h5.6v-.8c0-.5.2-1 .6-1.3A6 6 0 0 0 12 3z" />
      <line x1="9.5" y1="19" x2="14.5" y2="19" />
      <line x1="10.5" y1="21.5" x2="13.5" y2="21.5" />
    </>
  ),
  check: <polyline points="4.5 12.5 9.5 17.5 19.5 6.5" />,
  wrench: (
    <path d="M15.5 3.5a5.5 5.5 0 0 0-5 7.7L3.5 18.2l2.3 2.3 7-7a5.5 5.5 0 0 0 7.2-6.9l-3 3-2.6-2.6 3-3a5.5 5.5 0 0 0-1.9-.5z" />
  ),
  clock: (
    <>
      <circle cx="12" cy="12" r="8.5" />
      <polyline points="12 7 12 12 15.5 14" />
    </>
  ),
  lock: (
    <>
      <rect x="4.5" y="10.5" width="15" height="10" rx="3" />
      <path d="M8.5 10.5V7.8a3.5 3.5 0 0 1 7 0v2.7" />
    </>
  ),
  spark: (
    <>
      <path d="M12 3l1.9 5.6L19.5 10l-5.6 1.9L12 17.5l-1.9-5.6L4.5 10l5.6-1.4z" />
      <line x1="18.5" y1="17" x2="18.5" y2="21" />
      <line x1="16.5" y1="19" x2="20.5" y2="19" />
    </>
  ),
  dash: <line x1="5.5" y1="12" x2="18.5" y2="12" />,
  arrow: (
    <>
      <line x1="4.5" y1="12" x2="18" y2="12" />
      <polyline points="12.5 6.5 19 12 12.5 17.5" />
    </>
  ),
}

// Sized by the parent's font-size and coloured by its text colour.
export default function Icon({ name, className, style }) {
  return (
    <svg
      viewBox="0 0 24 24"
      width="1em"
      height="1em"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      className={className}
      style={{ flex: 'none', ...style }}
    >
      {ICONS[name]}
    </svg>
  )
}
