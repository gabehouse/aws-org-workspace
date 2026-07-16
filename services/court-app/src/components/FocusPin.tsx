import type { CSSProperties } from 'react'
import type { Court } from '../lib/data'
import { CourtPin } from './CourtPin'

interface FocusPinProps {
  court: Court
  x: number
  bottom: number
  openCount?: number
  active?: boolean
  label: string
  onClick: () => void
}

/** Lit court pin rendered above the match backdrop at map-projected coordinates. */
export function FocusPin({
  court,
  x,
  bottom,
  openCount = 0,
  active = false,
  label,
  onClick,
}: FocusPinProps) {
  const style = {
    left: x,
    top: bottom,
    transform: 'translate(-50%, -100%)',
  } satisfies CSSProperties

  return (
    <CourtPin
      as="button"
      court={court}
      openCount={openCount}
      active={active}
      focusLit
      title={label}
      onClick={onClick}
      style={style}
      className="map-focus-pin"
    />
  )
}
