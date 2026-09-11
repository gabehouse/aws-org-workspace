import { useEffect, useRef, useState, type CSSProperties } from 'react'
import {
  createChallenge,
  createGauntlet,
  fetchHandles,
  fetchOngoingMatchesAtCourt,
  fetchProfileByUserId,
  playerDisplayName,
  isValidChallengeStartTime,
  localChallengeInputMin,
  localInputValue,
  withdrawGauntlet,
  DEFAULT_FORMAT,
  DEFAULT_STAKES,
  type Court,
  type Challenge,
  type Gauntlet,
  type Match,
} from '../lib/data'
import { formatCeloLabel } from '../lib/celoDisplay'
import { isMatchViewed, markMatchesViewed } from '../lib/seenState'
import { useAuthGate } from '../lib/authGate'
import { MatchRulesPopover, RulesButton } from './MatchRulesPopover'
import { DirectionsButton } from './DirectionsButton'
import { MatchRow } from './MatchRow'
import { AdminCourtTools } from './AdminCourtTools'

interface CourtPanelProps {
  court: Court
  gauntlets: Gauntlet[]
  challenges: Challenge[]
  handles: Record<string, string>
  currentUserId: string
  onClose: () => void
  onGauntletDropped: () => void
  onChallengeSent: () => void | Promise<void>
  onViewProfile: (userId: string) => void
  onOpenMatch: (matchId: string) => void
  matchRefreshKey?: number
  matchSeenVersion?: number
  onMatchesSeen?: () => void
  panelStyle?: CSSProperties
  isAdmin?: boolean
  onCourtDeleted?: () => void
}

const OPEN_REQUEST_STATUSES = new Set(['PENDING', 'COUNTERED'])

export function CourtPanel({
  court,
  gauntlets,
  challenges,
  handles,
  currentUserId,
  onClose,
  onGauntletDropped,
  onChallengeSent,
  onViewProfile,
  onOpenMatch,
  matchRefreshKey = 0,
  matchSeenVersion = 0,
  onMatchesSeen,
  panelStyle,
  isAdmin = false,
  onCourtDeleted,
}: CourtPanelProps) {
  const { requireAuth } = useAuthGate()
  const signedIn = Boolean(currentUserId)
  const [showDropForm, setShowDropForm] = useState(false)
  const [courtMatches, setCourtMatches] = useState<Match[] | null>(null)
  const courtMatchesRef = useRef(courtMatches)
  courtMatchesRef.current = courtMatches
  const [matchHandles, setMatchHandles] = useState<Record<string, string>>({})
  const [showRules, setShowRules] = useState(false)
  const [note, setNote] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [ownerElos, setOwnerElos] = useState<Record<string, number>>({})
  const [ownerLabels, setOwnerLabels] = useState<Record<string, string>>({})

  // Which gauntlet has its challenge form open, plus that form's fields
  const [challengingId, setChallengingId] = useState<string | null>(null)
  const [proposedStart, setProposedStart] = useState(() => localInputValue(0))
  const inputMin = localChallengeInputMin()
  const [message, setMessage] = useState('')

  const isWaitingOnTheirReply = (gauntletId: string) =>
    challenges.some(
      (c) =>
        c.gauntletId === gauntletId &&
        c.challengerUserId === currentUserId &&
        OPEN_REQUEST_STATUSES.has(c.status) &&
        c.proposedByUserId === currentUserId,
    )

  const ownGauntlet = gauntlets.some((g) => g.ownerUserId === currentUserId)

  useEffect(() => {
    const ownerIds = [...new Set(gauntlets.map((g) => g.ownerUserId))]
    if (!ownerIds.length) {
      setOwnerElos({})
      setOwnerLabels({})
      return
    }
    let cancelled = false
    void Promise.all(
      ownerIds.map(async (userId) => {
        const profile = await fetchProfileByUserId(userId)
        return [userId, profile] as const
      }),
    ).then((pairs) => {
      if (cancelled) return
      setOwnerElos(
        Object.fromEntries(pairs.map(([userId, profile]) => [userId, profile?.globalElo ?? 1200])),
      )
      setOwnerLabels(
        Object.fromEntries(
          pairs
            .map(([userId, profile]) => [userId, playerDisplayName(profile)] as const)
            .filter((p): p is [string, string] => Boolean(p[1])),
        ),
      )
    })
    return () => {
      cancelled = true
    }
  }, [gauntlets])

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const shown = await fetchOngoingMatchesAtCourt(court.id)
        if (cancelled) return
        setCourtMatches(shown)
        const userIds = [
          ...new Set(shown.flatMap((m) => [m.defenderUserId, m.challengerUserId])),
        ]
        if (userIds.length) {
          const h = await fetchHandles(userIds)
          if (!cancelled) setMatchHandles(h)
        }
      } catch {
        if (!cancelled) setCourtMatches([])
      }
    })()
    return () => {
      cancelled = true
    }
  }, [court.id, matchRefreshKey])

  // Thumbnails count as seen when the panel closes or the user switches courts.
  useEffect(() => {
    return () => {
      if (!currentUserId) return
      const matches = courtMatchesRef.current
      if (!matches?.length) return
      markMatchesViewed(
        currentUserId,
        matches.map((m) => m.id),
      )
      onMatchesSeen?.()
    }
  }, [court.id, currentUserId, onMatchesSeen])

  const allHandles = { ...handles, ...matchHandles, ...ownerLabels }

  const dropGauntlet = async () => {
    if (!requireAuth()) return
    setSubmitting(true)
    setFormError(null)
    try {
      await createGauntlet({
        ownerUserId: currentUserId,
        courtId: court.id,
        stakes: DEFAULT_STAKES,
        format: DEFAULT_FORMAT,
        note: note.trim() || undefined,
      })
      setShowDropForm(false)
      setNote('')
      onGauntletDropped()
    } catch (e) {
      setFormError(e instanceof Error ? e.message : 'Failed to post challenge')
    } finally {
      setSubmitting(false)
    }
  }

  const sendChallenge = async (gauntlet: Gauntlet) => {
    if (!requireAuth()) return
    const start = new Date(proposedStart)
    if (!isValidChallengeStartTime(start.toISOString())) {
      setFormError('Pick a time from now on (play-now is OK)')
      return
    }
    setSubmitting(true)
    setFormError(null)
    try {
      await createChallenge({
        gauntlet,
        challengerUserId: currentUserId,
        proposedStart: start.toISOString(),
        message: message.trim() || undefined,
      })
      setChallengingId(null)
      setMessage('')
      await onChallengeSent()
    } catch (e) {
      setFormError(e instanceof Error ? e.message : 'Failed to send request')
    } finally {
      setSubmitting(false)
    }
  }

  const removeGauntlet = async (gauntlet: Gauntlet) => {
    setSubmitting(true)
    setFormError(null)
    try {
      await withdrawGauntlet(gauntlet, currentUserId)
      onGauntletDropped()
    } catch (e) {
      setFormError(e instanceof Error ? e.message : 'Failed to remove challenge')
    } finally {
      setSubmitting(false)
    }
  }

  const details = [
    court.surface && court.surface.charAt(0) + court.surface.slice(1).toLowerCase(),
    court.courtCount && court.courtCount > 1 ? `${court.courtCount} courts` : null,
    court.hasLights ? 'Lights' : null,
  ].filter(Boolean)

  return (
    <aside
      className={`panel panel--court${panelStyle ? ' panel--anchored' : ''}`}
      style={panelStyle}
    >
      <div className="panel__header">
        <div>
          <h2>{court.name}</h2>
          {details.length > 0 && <p className="panel__meta">{details.join(' · ')}</p>}
          {court.address && <p className="panel__meta">{court.address}</p>}
        </div>
        <div className="panel__header-actions">
          <DirectionsButton lat={court.lat} lng={court.lng} compact />
          <button type="button" className="btn btn--icon" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>
      </div>

      {formError && <p className="form-error">{formError}</p>}

      <MatchRulesPopover open={showRules} onClose={() => setShowRules(false)} />

      <section className="panel__section">
        <h3>Matches here</h3>
        {courtMatches === null && <p className="panel__empty">Loading…</p>}
        {courtMatches?.length === 0 && (
          <p className="panel__empty">No active or pending matches at this court.</p>
        )}
        {courtMatches?.map((m) => (
          <MatchRow
            key={m.id}
            match={m}
            handles={allHandles}
            isNew={
              signedIn && matchSeenVersion >= 0 && !isMatchViewed(currentUserId, m.id)
            }
            layout="court"
            onOpenMatch={(matchId) => {
              if (!requireAuth(() => onOpenMatch(matchId))) return
            }}
            onViewProfile={onViewProfile}
          />
        ))}
      </section>

      <section className="panel__section">
        <h3>Open challenges</h3>
        {gauntlets.length === 0 && (
          <p className="panel__empty">No open challenges here. Post one below.</p>
        )}
        {gauntlets.map((g) => {
          const isMine = g.ownerUserId === currentUserId
          return (
            <div key={g.id} className="gauntlet-card">
              <div className="gauntlet-card__header">
                <div className="gauntlet-card__owner">
                  <button
                    type="button"
                    className="name-link"
                    onClick={() => onViewProfile(g.ownerUserId)}
                  >
                    {allHandles[g.ownerUserId] ?? 'Unknown player'}
                  </button>
                  {isMine && ' (you)'}
                </div>
                {isMine && (
                  <button
                    type="button"
                    className="btn btn--icon gauntlet-card__remove"
                    aria-label="Remove your challenge"
                    disabled={submitting}
                    onClick={() => void removeGauntlet(g)}
                  >
                    ×
                  </button>
                )}
              </div>
              <div className="gauntlet-card__elo">
                {formatCeloLabel(ownerElos[g.ownerUserId] ?? 1200)}
              </div>
              {g.note && <div className="gauntlet-card__note">“{g.note}”</div>}

              {!isMine && isWaitingOnTheirReply(g.id) && (
                <p className="gauntlet-card__sent">
                  Request sent — watch for their reply under Requests.
                </p>
              )}

              {!isMine && !isWaitingOnTheirReply(g.id) && challengingId !== g.id && (
                <button
                  type="button"
                  className="btn btn--primary btn--small"
                  onClick={() => {
                    requireAuth(() => {
                      setChallengingId(g.id)
                      setFormError(null)
                    })
                  }}
                >
                  Request match
                </button>
              )}

              {challengingId === g.id && (
                <form
                  className="gauntlet-form"
                  onSubmit={(e) => {
                    e.preventDefault()
                    void sendChallenge(g)
                  }}
                >
                  <label>
                    Propose a time
                    <input
                      type="datetime-local"
                      value={proposedStart}
                      min={inputMin}
                      onChange={(e) => setProposedStart(e.target.value)}
                      required
                    />
                  </label>
                  <label>
                    Message (optional)
                    <input
                      type="text"
                      value={message}
                      onChange={(e) => setMessage(e.target.value)}
                      placeholder="e.g. I'm free after work"
                    />
                  </label>
                  {formError && <p className="form-error">{formError}</p>}
                  <div className="gauntlet-form__actions">
                    <button type="submit" className="btn btn--primary" disabled={submitting}>
                      {submitting ? 'Sending…' : 'Send request'}
                    </button>
                    <button
                      type="button"
                      className="btn btn--ghost"
                      onClick={() => setChallengingId(null)}
                      disabled={submitting}
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              )}
            </div>
          )
        })}
      </section>

      {!showDropForm ? (
        <button
          type="button"
          className="btn btn--primary"
          onClick={() => {
            requireAuth(() => {
              setShowDropForm(true)
              setFormError(null)
            })
          }}
          disabled={signedIn && ownGauntlet}
        >
          {signedIn && ownGauntlet ? 'You posted a challenge here' : 'Post challenge'}
        </button>
      ) : (
        <form
          className="panel__section gauntlet-form"
          onSubmit={(e) => {
            e.preventDefault()
            void dropGauntlet()
          }}
        >
          <h3>Post challenge</h3>
          <p className="panel__meta">
            No schedule needed — others send a request with a time and you nudge it
            until it works.
          </p>
          <RulesButton onClick={() => setShowRules(true)} />
          <label>
            Note (optional)
            <input
              type="text"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="e.g. evenings preferred"
            />
          </label>
          {formError && <p className="form-error">{formError}</p>}
          <div className="gauntlet-form__actions">
            <button type="submit" className="btn btn--primary" disabled={submitting}>
              {submitting ? 'Posting…' : 'Post challenge'}
            </button>
            <button
              type="button"
              className="btn btn--ghost"
              onClick={() => setShowDropForm(false)}
              disabled={submitting}
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {isAdmin && onCourtDeleted && (
        <AdminCourtTools court={court} onDeleted={onCourtDeleted} />
      )}
    </aside>
  )
}
