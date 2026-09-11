import type { CSSProperties } from 'react'
import type { Court } from '../lib/data'
import { CourtMarker } from './CourtMarker'

interface FocusPinProps {
  court: Court
  x: number
  bottom: number
  matchCount?: number
  openCount?: number
  active?: boolean
  label: string
  /** Zoom-linked scale; 1 at city zoom. */
  scale?: number
  onClick: () => void
}

/** Lit court marker rendered above the match backdrop at map-projected coordinates. */
export function FocusPin({
  court,
  x,
  bottom,
  matchCount = 0,
  openCount = 0,
  active = false,
  label,
  scale = 1,
  onClick,
}: FocusPinProps) {
  const style = {
    left: x,
    top: bottom,
    transform: `translate(-50%, -100%) scale(${scale})`,
    transformOrigin: 'bottom center',
  } satisfies CSSProperties

  return (
    <CourtMarker
      as="button"
      court={court}
      matchCount={matchCount}
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
