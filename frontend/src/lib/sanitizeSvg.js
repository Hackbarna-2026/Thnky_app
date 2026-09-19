const BLOCKED = 'script, foreignObject, iframe, object, embed'

// Challenge figures are SVG strings from the server. They come from the static bank (the
// model does not generate them), but model output is untrusted by rule (CLAUDE.md section 11),
// so this strips anything executable before the markup reaches the DOM.
export function sanitizeSvg(markup) {
  if (typeof markup !== 'string') return ''

  const doc = new DOMParser().parseFromString(markup, 'image/svg+xml')
  const svg = doc.documentElement
  if (doc.querySelector('parsererror') || svg.nodeName.toLowerCase() !== 'svg') return ''

  doc.querySelectorAll(BLOCKED).forEach((node) => node.remove())
  doc.querySelectorAll('*').forEach((el) => {
    for (const attr of [...el.attributes]) {
      const name = attr.name.toLowerCase()
      const value = attr.value.trim().toLowerCase()
      const isScriptUrl = (name === 'href' || name === 'xlink:href') && value.startsWith('javascript:')
      if (name.startsWith('on') || isScriptUrl) el.removeAttribute(attr.name)
    }
  })
  return new XMLSerializer().serializeToString(svg)
}
