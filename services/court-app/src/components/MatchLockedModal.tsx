import type { Court, Match } from '../lib/data'
import { formatDateTime } from '../lib/data'
import { googleCalendarUrlForMatch } from '../lib/calendar'
import { AddToCalendarButton } from './AddToCalendarButton'

interface MatchLockedModalProps {
  match: Match
  court: Court | null
  opponentHandle: string
  handles: Record<string, string>
  onClose: () => void
  onOpenMatch: () => void
}

export function MatchLockedModal({
  match,
  court,
  opponentHandle,
  handles,
  onClose,
  onOpenMatch,
}: MatchLockedModalProps) {
  const calendarUrl = googleCalendarUrlForMatch(match, court, handles)

  return (
    <div className="match-locked-modal" role="dialog" aria-labelledby="match-locked-title">
      <div className="match-locked-modal__icon" aria-hidden>
        ✓
      </div>
      <h2 id="match-locked-title" className="match-locked-modal__title">
        Match locked in!
      </h2>
      <p className="match-locked-modal__body">
        You accepted {opponentHandle}&apos;s request
        {court ? (
          <>
            {' '}
            at <strong>{court.name}</strong>
          </>
        ) : null}
        .
      </p>
      <p className="match-locked-modal__when">{formatDateTime(match.scheduledAt)}</p>
      <div className="match-locked-modal__actions">
        <AddToCalendarButton url={calendarUrl} />
        <button type="button" className="btn btn--ghost" onClick={onOpenMatch}>
          Open match
        </button>
        <button type="button" className="btn btn--ghost btn--small" onClick={onClose}>
          Done
        </button>
      </div>
    </div>
  )
}
