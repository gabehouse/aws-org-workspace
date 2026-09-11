import { DEFAULT_FORMAT, DEFAULT_STAKES } from '../lib/data'

const COURT_SETUP_RULES =
  'Spin a racquet before warming up:\n• Winner of the spin chooses who serves first.\n• Loser of the spin chooses which side of the court to start on.'

const SCORING_RULES =
  'Best of 3 (ATP deuce) — match ends at 2 sets won. Valid scores: 6-0 through 6-4, 7-5, or 7-6 with tiebreak. Leave set 3 blank for a 2-0 match.'

interface MatchRulesPopoverProps {
  open: boolean
  onClose: () => void
  stakes?: string | null
  format?: string | null
  showAfterPlay?: boolean
}

export function RulesButton({
  onClick,
  className = '',
}: {
  onClick: () => void
  className?: string
}) {
  return (
    <button
      type="button"
      className={`btn btn--rules btn--small${className ? ` ${className}` : ''}`}
      onClick={onClick}
    >
      Rules
    </button>
  )
}

export function MatchRulesPopover({
  open,
  onClose,
  stakes,
  format,
  showAfterPlay = false,
}: MatchRulesPopoverProps) {
  if (!open) return null

  return (
    <div className="match-rules">
      <button
        type="button"
        className="match-rules__backdrop"
        aria-label="Close rules"
        onClick={onClose}
      />
      <div className="match-rules__card" role="dialog" aria-labelledby="match-rules-title">
        <div className="match-rules__header">
          <h3 id="match-rules-title">Rules & stakes</h3>
          <button type="button" className="btn btn--icon" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>
        <div className="match-rules__body">
          <section>
            <h4>Stakes</h4>
            <p>{stakes || DEFAULT_STAKES}</p>
          </section>
          <section>
            <h4>Format</h4>
            <p>{format || DEFAULT_FORMAT}</p>
          </section>
          <section>
            <h4>Court Setup</h4>
            <p className="match-rules__multiline">{COURT_SETUP_RULES}</p>
          </section>
          <section>
            <h4>Scoring</h4>
            <p>{SCORING_RULES}</p>
          </section>
          {showAfterPlay && (
            <section>
              <h4>After you play</h4>
              <p>The winner posts the score within 24 hours. No check-in needed.</p>
            </section>
          )}
        </div>
      </div>
    </div>
  )
}
