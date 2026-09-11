import type { MouseEvent } from 'react'
import { openGoogleCalendar } from '../lib/calendar'

interface AddToCalendarButtonProps {
  url: string
  /** Icon-only for match rows */
  compact?: boolean
  className?: string
  onClick?: (e: MouseEvent<HTMLButtonElement>) => void
}

export function AddToCalendarButton({
  url,
  compact,
  className = '',
  onClick,
}: AddToCalendarButtonProps) {
  return (
    <button
      type="button"
      className={`btn btn--calendar${compact ? ' btn--calendar-compact' : ''}${className ? ` ${className}` : ''}`}
      title="Add to Google Calendar"
      aria-label="Add to Google Calendar"
      onClick={(e) => {
        onClick?.(e)
        e.stopPropagation()
        openGoogleCalendar(url)
      }}
    >
      {compact ? (
        <span className="btn--calendar__icon" aria-hidden>
          📅
        </span>
      ) : (
        'Add to Google Calendar'
      )}
    </button>
  )
}
