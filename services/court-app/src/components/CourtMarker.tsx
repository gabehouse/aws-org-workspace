import type { CSSProperties, ReactNode } from 'react'

/** Tennis ball paths (Capa-style) — fill uses `--ball-yellow` to match menu toggles. */
export function CourtBallIcon() {
  return (
    <svg
      className="court-marker__ball"
      viewBox="0 0 879.792 879.792"
      aria-hidden="true"
    >
      <path d="M122.711,135.041c-168.3,175.1-162.8,453.6,12.3,622c175.101,168.299,453.601,162.799,622-12.301 c86.601-90.1,127.2-207.5,122.4-323.2c-41,23.1-78.101,51.6-110.7,85.299c-18.5,19.102-35,39.701-52.5,61.602 c-17.2,21.5-34.9,43.6-54.8,64.299c-54.3,56.5-127.3,88.4-205.7,90c-2,0-4,0.1-6,0.1c-76.1,0-148.1-28.898-203.1-81.799 c-56.5-54.301-88.4-127.301-90-205.701c-1.6-78.3,27.5-152.6,81.8-209.1c19.8-20.6,41.3-39.3,62.1-57.3 c21.101-18.3,41.101-35.6,59.5-54.9c32.3-33.9,59.4-72.1,80.8-114C324.911-0.259,209.211,44.941,122.711,135.041z" />
      <path d="M336.511,210.341c-20.7,18-40.3,35-58.5,53.8c-44.1,45.9-67.7,106.2-66.5,169.9c1.3,63.6,27.2,123,73.101,167.1 c45.899,44.1,106.199,67.699,169.899,66.5c63.601-1.301,123-27.199,167.101-73.1c18.1-18.801,34.3-39.1,51.5-60.5 c17.5-21.9,35.6-44.5,55.899-65.5c41.601-43,90-78.6,143.7-105.9c-15.7-88.4-58.5-172.8-128.2-239.8 c-69.6-67.1-155.6-106.6-244.6-118.8c-25.2,54.8-58.8,104.5-100.101,147.8C379.611,172.941,357.711,191.941,336.511,210.341z" />
    </svg>
  )
}

/** MapLibre markers are isolated stacking contexts; split pin/label so titles can sit above other pins. */
export type CourtMarkerPart = 'all' | 'pin' | 'label'

type CourtMarkerProps = {
  court?: { name: string }
  matchCount?: number
  openCount?: number
  selected?: boolean
  active?: boolean
  underOverlay?: boolean
  focusLit?: boolean
  draft?: boolean
  /** Render full marker, pin only, or label only (for map z-index layering). */
  part?: CourtMarkerPart
  className?: string
  title?: string
  onClick?: () => void
  as?: 'button' | 'div'
  style?: CSSProperties
  children?: ReactNode
}

export function CourtMarker({
  court,
  matchCount = 0,
  openCount = 0,
  selected = false,
  active = false,
  underOverlay = false,
  focusLit = false,
  draft = false,
  part = 'all',
  className = '',
  title,
  onClick,
  as = 'div',
  style,
  children,
}: CourtMarkerProps) {
  const hasActivity = active || matchCount > 0 || openCount > 0
  const elevated = selected || focusLit
  const label = court?.name?.trim()
  const showLabel = part === 'all' || part === 'label'
  const showPin = part === 'all' || part === 'pin'

  const classes = [
    'court-marker',
    part === 'pin' ? 'court-marker--pin-only' : '',
    part === 'label' ? 'court-marker--label-only' : '',
    hasActivity ? 'court-marker--active' : '',
    elevated ? 'court-marker--selected' : '',
    underOverlay ? 'court-marker--under-overlay' : '',
    draft ? 'court-marker--draft' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ')

  const content = (
    <>
      {showLabel && label && !draft && <span className="court-marker__label">{label}</span>}
      {showLabel && draft && (
        <span className="court-marker__label court-marker__label--draft">New court</span>
      )}
      {showPin && (
        <div className="court-marker__pin">
          <CourtBallIcon />
          {(matchCount > 0 || openCount > 0) && (
            <span className="court-marker__badges">
              {matchCount > 0 && (
                <span
                  className="court-marker__badge court-marker__badge--matches"
                  aria-label={`${matchCount} ongoing matches`}
                >
                  {matchCount}
                </span>
              )}
              {openCount > 0 && (
                <span
                  className="court-marker__badge court-marker__badge--challenges"
                  aria-label={`${openCount} open challenges`}
                >
                  {openCount}
                </span>
              )}
            </span>
          )}
        </div>
      )}
      {children}
    </>
  )

  const shared = {
    className: classes,
    style,
    title: title ?? court?.name,
    children: content,
  }

  if (as === 'button') {
    return (
      <button
        type="button"
        onClick={onClick}
        aria-label={court?.name ?? 'Court'}
        {...shared}
      />
    )
  }

  return <div {...shared} />
}
