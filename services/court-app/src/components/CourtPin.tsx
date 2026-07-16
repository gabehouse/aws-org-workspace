import type { CSSProperties, ReactNode } from 'react'

type CourtPinProps = {
  court?: { name: string }
  openCount?: number
  selected?: boolean
  active?: boolean
  underOverlay?: boolean
  focusLit?: boolean
  draft?: boolean
  className?: string
  title?: string
  onClick?: () => void
  as?: 'button' | 'div'
  style?: CSSProperties
  children?: ReactNode
}

export function CourtPin({
  court,
  openCount = 0,
  selected = false,
  active = false,
  underOverlay = false,
  focusLit = false,
  draft = false,
  className = '',
  title,
  onClick,
  as = 'div',
  style,
  children,
}: CourtPinProps) {
  const classes = [
    'court-pin',
    active ? 'court-pin--active' : '',
    selected ? 'court-pin--selected' : '',
    underOverlay ? 'court-pin--under-overlay' : '',
    focusLit ? 'court-pin--focus-lit' : '',
    draft ? 'court-pin--draft' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ')

  const graphic = draft ? (
    <span className="court-pin__draft-marker" aria-hidden="true">
      <svg viewBox="0 0 24 24" width="18" height="18">
        <path
          d="M12 2c3.3 0 6 2.7 6 6 0 4.5-6 12-6 12S6 12.5 6 8c0-3.3 2.7-6 6-6z"
          fill="var(--court-green)"
          stroke="var(--court-green-dark)"
          strokeWidth="1.2"
        />
        <circle cx="12" cy="8" r="2" fill="#fff" />
      </svg>
    </span>
  ) : (
    <>
      <span className="court-pin__icon" aria-hidden="true">
        🎾
      </span>
      {openCount > 0 && (
        <span className="court-pin__badge" aria-label={`${openCount} open requests`}>
          {openCount}
        </span>
      )}
    </>
  )

  const shared = {
    className: classes,
    style,
    title: title ?? court?.name,
    children: (
      <>
        {graphic}
        {children}
      </>
    ),
  }

  if (as === 'button') {
    return (
      <button type="button" onClick={onClick} aria-label={court?.name ?? 'Court'} {...shared} />
    )
  }

  return <div {...shared} />
}
