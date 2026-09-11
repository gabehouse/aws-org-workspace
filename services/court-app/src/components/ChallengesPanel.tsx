import { useEffect, useRef, useState } from 'react'
import {
  acceptChallenge,
  canAcceptChallenge,
  client,
  closeChallenge,
  fetchDispute,
  fetchHandles,
  fetchMatchByChallengeId,
  fetchMyOngoingMatches,
  formatDateTime,
  isOngoingMatch,
  isValidChallengeStartTime,
  localDayInputBounds,
  localInputValue,
  notifyMatchLocked,
  nudgeChallenge,
  reconcileAcceptedChallenge,
  requestMatchBucket,
  type Challenge,
  type Court,
  type Dispute,
  type Match,
} from '../lib/data'
import { googleCalendarUrlForMatch } from '../lib/calendar'
import { isChallengeViewed, isMatchViewed, markMatchesViewed } from '../lib/seenState'
import { MatchLockedModal } from './MatchLockedModal'
import { MatchRow } from './MatchRow'

const OPEN_STATUSES = ['PENDING', 'COUNTERED'] as const

interface ChallengesPanelProps {
  challenges: Challenge[]
  courts: Record<string, Court>
  currentUserId: string
  onClose: () => void
  /** Reload challenges (and gauntlets, since accepting locks one) */
  onChanged: () => void
  onViewProfile: (userId: string) => void
  onOpenMatch: (matchId: string) => void
  onOpenCourt?: (courtId: string) => void
  matchSeenVersion?: number
  challengeSeenVersion?: number
  onMatchesSeen?: () => void
}

export function ChallengesPanel({
  challenges,
  courts,
  currentUserId,
  onClose,
  onChanged,
  onViewProfile,
  onOpenMatch,
  onOpenCourt,
  matchSeenVersion = 0,
  challengeSeenVersion = 0,
  onMatchesSeen,
}: ChallengesPanelProps) {
  const [handles, setHandles] = useState<Record<string, string>>({})
  const scheduledMatchIdsRef = useRef<string[]>([])
  // gauntletId -> courtId, resolved for challenges whose gauntlet isn't loaded
  const [gauntletCourts, setGauntletCourts] = useState<Record<string, string>>({})
  const [nudgingId, setNudgingId] = useState<string | null>(null)
  const [nudgeStart, setNudgeStart] = useState(() => localInputValue(0))
  const dayBounds = localDayInputBounds()
  const [busyId, setBusyId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [matchByChallenge, setMatchByChallenge] = useState<Record<string, Match | null>>({})
  const [disputes, setDisputes] = useState<Record<string, Dispute | null>>({})
  const [acceptSuccess, setAcceptSuccess] = useState<{
    match: Match
    challenge: Challenge
  } | null>(null)

  useEffect(() => {
    const others = [
      ...new Set(
        challenges.map((c) =>
          c.challengerUserId === currentUserId ? c.defenderUserId : c.challengerUserId,
        ),
      ),
    ]
    if (others.length) {
      void fetchHandles(others).then((h) =>
        setHandles((prev) => ({ ...prev, ...h })),
      )
    }
    const gauntletIds = [...new Set(challenges.map((c) => c.gauntletId))]
    const accepted = challenges.filter((c) => c.status === 'ACCEPTED')
    void Promise.all([
      Promise.all(
        gauntletIds.map(async (id) => {
          const { data } = await client.models.Gauntlet.get({ id })
          return [id, data?.courtId] as const
        }),
      ),
      Promise.all(
        accepted.map(async (c) => {
          const m = await fetchMatchByChallengeId(c.id)
          return [c.id, m] as const
        }),
      ),
      fetchMyOngoingMatches(currentUserId),
    ]).then(async ([gauntletPairs, matchPairs, ongoingMatches]) => {
      setGauntletCourts(
        Object.fromEntries(gauntletPairs.filter((p): p is [string, string] => Boolean(p[1]))),
      )
      const matchMap: Record<string, Match | null> = Object.fromEntries(matchPairs)
      for (const m of ongoingMatches) {
        if (m.challengeId) matchMap[m.challengeId] = m
      }
      setMatchByChallenge(matchMap)

      const disputed = Object.values(matchMap)
        .filter((m): m is Match => m !== null && m.status === 'DISPUTED')
      if (disputed.length) {
        const disputePairs = await Promise.all(
          disputed.map(async (m) => [m.id, await fetchDispute(m.id)] as const),
        )
        setDisputes(Object.fromEntries(disputePairs))
      } else {
        setDisputes({})
      }

      let stale = false
      for (const c of accepted) {
        const updated = await reconcileAcceptedChallenge(c)
        if (updated.status !== c.status) stale = true
      }
      if (stale) onChanged()
    })
  }, [challenges, currentUserId, onChanged])

  const courtName = (c: Challenge) => {
    const courtId = gauntletCourts[c.gauntletId]
    return (courtId && courts[courtId]?.name) || 'Unknown court'
  }
  const courtIdFor = (c: Challenge) => gauntletCourts[c.gauntletId] ?? null
  const courtLink = (c: Challenge) => {
    const courtId = courtIdFor(c)
    const name = courtName(c)
    if (courtId && onOpenCourt) {
      return (
        <button
          type="button"
          className="name-link"
          onClick={() => onOpenCourt(courtId)}
        >
          {name}
        </button>
      )
    }
    return name
  }
  const otherPlayerId = (c: Challenge) =>
    c.challengerUserId === currentUserId ? c.defenderUserId : c.challengerUserId

  const act = async (id: string, fn: () => Promise<void>) => {
    setBusyId(id)
    setError(null)
    try {
      await fn()
      setNudgingId(null)
      onChanged()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Action failed')
    } finally {
      setBusyId(null)
    }
  }

  const acceptAndNotify = async (c: Challenge) => {
    setBusyId(c.id)
    setError(null)
    try {
      const match = await acceptChallenge(c, currentUserId)
      const courtId = gauntletCourts[c.gauntletId]
      const court = courtId ? courts[courtId] ?? null : null
      const opponentId = otherPlayerId(c)
      const notifyHandles = await fetchHandles([currentUserId, opponentId])
      const accepterHandle = notifyHandles[currentUserId] ?? 'Your opponent'
      const calendarUrl = googleCalendarUrlForMatch(match, court, {
        ...handles,
        ...notifyHandles,
      })

      setAcceptSuccess({ match, challenge: c })
      onChanged()

      void notifyMatchLocked({
        recipientUserId: opponentId,
        accepterHandle,
        courtName: court?.name ?? 'the court',
        scheduledAt: match.scheduledAt,
        calendarUrl,
        format: match.format,
        stakes: match.stakes,
      }).catch(() => {
        // Email is best-effort; acceptance already succeeded
      })
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Action failed')
    } finally {
      setBusyId(null)
    }
  }

  const open = challenges.filter((c) =>
    (OPEN_STATUSES as readonly string[]).includes(c.status),
  )
  const yourMove = open.filter((c) => c.proposedByUserId !== currentUserId)
  const theirMove = open.filter((c) => c.proposedByUserId === currentUserId)

  const ongoingMatchChallenges = challenges.filter((c) => {
    const m = matchByChallenge[c.id]
    if (!m) return c.status === 'ACCEPTED'
    return isOngoingMatch(m)
  })

  const bucketForChallenge = (c: Challenge) => {
    const m = matchByChallenge[c.id]
    if (!m) return 'scheduled' as const
    return requestMatchBucket(m, currentUserId, { disputeStatus: disputes[m.id]?.status })
  }

  const yourMoveMatches = ongoingMatchChallenges.filter(
    (c) => bucketForChallenge(c) === 'your-move',
  )
  const theirMoveMatches = ongoingMatchChallenges.filter(
    (c) => bucketForChallenge(c) === 'waiting-on-them',
  )
  const scheduledMatches = ongoingMatchChallenges.filter((c) => {
    const m = matchByChallenge[c.id]
    return !m || bucketForChallenge(c) === 'scheduled'
  })

  scheduledMatchIdsRef.current = ongoingMatchChallenges
    .map((c) => matchByChallenge[c.id]?.id)
    .filter((id): id is string => Boolean(id))

  useEffect(() => {
    return () => {
      if (scheduledMatchIdsRef.current.length === 0) return
      markMatchesViewed(currentUserId, scheduledMatchIdsRef.current)
      onMatchesSeen?.()
    }
  }, [currentUserId, onMatchesSeen])

  const renderCard = (c: Challenge, mode: 'yours' | 'theirs') => {
    const isNew =
      mode === 'yours' &&
      challengeSeenVersion >= 0 &&
      !isChallengeViewed(currentUserId, c.id)

    return (
    <div key={c.id} className="gauntlet-card">
      <div className="gauntlet-card__owner">
        <button
          type="button"
          className="name-link"
          onClick={() => onViewProfile(otherPlayerId(c))}
        >
          {handles[otherPlayerId(c)] ?? 'Unknown player'}
        </button>{' '}
        · {courtLink(c)}
        {isNew && <span className="gauntlet-card__new">New</span>}
      </div>
      <div className="gauntlet-card__window">
        {formatDateTime(c.proposedStart)}
        {c.proposedByUserId === currentUserId ? ' — you proposed' : ' — they proposed'}
      </div>
      {c.message && <div className="gauntlet-card__note">“{c.message}”</div>}

      {mode === 'yours' && !canAcceptChallenge(c) && (
        <p className="panel__meta">
          Only same-day times can be accepted — nudge to today or decline.
        </p>
      )}

      {mode === 'yours' && nudgingId !== c.id && (
        <div className="gauntlet-form__actions">
          <button
            type="button"
            className="btn btn--primary btn--small"
            disabled={busyId === c.id || !canAcceptChallenge(c)}
            onClick={() => void acceptAndNotify(c)}
          >
            {busyId === c.id ? 'Working…' : 'Accept'}
          </button>
          <button
            type="button"
            className="btn btn--ghost btn--small"
            disabled={busyId === c.id}
            onClick={() => {
              setNudgingId(c.id)
              setNudgeStart(localInputValue(0))
            }}
          >
            Nudge time
          </button>
          <button
            type="button"
            className="btn btn--ghost btn--small"
            disabled={busyId === c.id}
            onClick={() => void act(c.id, () => closeChallenge(c, 'DECLINED'))}
          >
            Decline
          </button>
        </div>
      )}

      {mode === 'yours' && nudgingId === c.id && (
        <form
          className="gauntlet-form"
          onSubmit={(e) => {
            e.preventDefault()
            const start = new Date(nudgeStart)
            if (!isValidChallengeStartTime(start.toISOString())) {
              setError('Pick a time today (play-now times are OK)')
              return
            }
            void act(c.id, () => nudgeChallenge(c, currentUserId, start.toISOString()))
          }}
        >
          <label>
            Works better for me
            <input
              type="datetime-local"
              value={nudgeStart}
              min={dayBounds.min}
              max={dayBounds.max}
              onChange={(e) => setNudgeStart(e.target.value)}
              required
            />
          </label>
          <div className="gauntlet-form__actions">
            <button type="submit" className="btn btn--primary btn--small" disabled={busyId === c.id}>
              {busyId === c.id ? 'Sending…' : 'Send'}
            </button>
            <button
              type="button"
              className="btn btn--ghost btn--small"
              onClick={() => setNudgingId(null)}
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {mode === 'theirs' && (
        <div className="gauntlet-form__actions">
          <button
            type="button"
            className="btn btn--ghost btn--small"
            disabled={busyId === c.id}
            onClick={() => void act(c.id, () => closeChallenge(c, 'WITHDRAWN'))}
          >
            {busyId === c.id ? 'Working…' : 'Withdraw'}
          </button>
        </div>
      )}
    </div>
    )
  }

  const renderScheduledMatch = (c: Challenge) => {
    const match = matchByChallenge[c.id]
    if (!match) {
      return (
        <div key={c.id} className="gauntlet-card">
          <div className="gauntlet-card__owner">
            <button
              type="button"
              className="name-link"
              onClick={() => onViewProfile(otherPlayerId(c))}
            >
              {handles[otherPlayerId(c)] ?? 'Unknown player'}
            </button>{' '}
            · {courtLink(c)}
          </div>
          <p className="panel__empty">Loading match…</p>
        </div>
      )
    }

    const isNew =
      matchSeenVersion >= 0 && !isMatchViewed(currentUserId, match.id)

    return (
      <MatchRow
        key={c.id}
        match={match}
        handles={handles}
        viewerUserId={currentUserId}
        dispute={disputes[match.id]}
        isNew={isNew}
        onOpenMatch={onOpenMatch}
        onViewProfile={onViewProfile}
      />
    )
  }

  return (
    <aside className="panel panel--challenges">
      <div className="panel__header">
        <h2>Requests</h2>
        <button type="button" className="btn btn--icon" onClick={onClose} aria-label="Close">
          ×
        </button>
      </div>

      {error && <p className="form-error">{error}</p>}

      <section className="panel__section">
        <h3>Your move</h3>
        {yourMove.length === 0 && yourMoveMatches.length === 0 && (
          <p className="panel__empty">Nothing waiting on you.</p>
        )}
        {yourMove.map((c) => renderCard(c, 'yours'))}
        {yourMoveMatches.map((c) => renderScheduledMatch(c))}
      </section>

      <section className="panel__section">
        <h3>Waiting on them</h3>
        {theirMove.length === 0 && theirMoveMatches.length === 0 && (
          <p className="panel__empty">No open offers from you.</p>
        )}
        {theirMove.map((c) => renderCard(c, 'theirs'))}
        {theirMoveMatches.map((c) => renderScheduledMatch(c))}
      </section>

      {scheduledMatches.length > 0 && (
        <section className="panel__section">
          <h3>Scheduled matches</h3>
          {scheduledMatches.map((c) => renderScheduledMatch(c))}
        </section>
      )}

      {acceptSuccess && (
        <div className="match-locked-overlay" onClick={() => setAcceptSuccess(null)}>
          <div onClick={(e) => e.stopPropagation()}>
            <MatchLockedModal
              match={acceptSuccess.match}
              court={(() => {
                const courtId = gauntletCourts[acceptSuccess.challenge.gauntletId]
                return courtId ? courts[courtId] ?? null : null
              })()}
              opponentHandle={
                handles[otherPlayerId(acceptSuccess.challenge)] ?? 'your opponent'
              }
              handles={handles}
              onClose={() => setAcceptSuccess(null)}
              onOpenMatch={() => {
                onOpenMatch(acceptSuccess.match.id)
                setAcceptSuccess(null)
              }}
            />
          </div>
        </div>
      )}
    </aside>
  )
}
