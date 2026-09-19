import { useMemo } from 'react'
import { sanitizeSvg } from '../../lib/sanitizeSvg.js'

// Renders a server-provided SVG string, sanitized first.
export default function SvgFigure({ markup, className }) {
  const html = useMemo(() => sanitizeSvg(markup), [markup])
  if (!html) return null
  return <div className={className} dangerouslySetInnerHTML={{ __html: html }} />
}
