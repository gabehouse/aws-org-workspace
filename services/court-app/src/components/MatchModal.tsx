import { useCallback, useEffect, useRef, useState, type CSSProperties } from 'react'
import {
  amendDisputedScore,
  autoConfirmScoreIfDue,
  cancelMatch,
  canPostScore,
  confirmPostedScore,
  disputePostedScore,
  doubleDownOnDisputedScore,
  fetchDispute,
  fetchHandles,
  fetchMatch,
  fetchMatchMessages,
  fetchProfileByUserId,
  fetchScoreReports,
  formatDateTime,
  scoreConfirmDeadline,
  scorePostDeadline,
  sendMatchMessage,
  submitScoreReport,
  type Court,
  type Dispute,
  type Match,
  type MatchMessage,
  type ScoreReport,
} from '../lib/data'
import {
  buildSetScores,
  DEFAULT_SET_INPUTS,
  formatAllSets,
  needsTiebreakInput,
  parseSetScores,
  setScoresToSetInputs,
  type SetInput,
} from '../lib/scores'
import { googleCalendarUrlForMatch } from '../lib/calendar'
import { AddToCalendarButton } from './AddToCalendarButton'
import { MatchRulesPopover, RulesButton } from './MatchRulesPopover'
import { AdminMatchTools } from './AdminMatchTools'

interface MatchModalProps {
  matchId: string
  currentUserId: string
  isAdmin?: boolean
  courts: Record<string, Court>
  onClose: () => void
  onChanged: () => void
  onViewProfile: (userId: string) => void
  panelStyle?: CSSProperties
}

export function MatchModal({
  matchId,
  currentUserId,
  isAdmin = false,
  courts,
  onClose,
  onChanged,
  onViewProfile,
  panelStyle,
}: MatchModalProps) {
  const panelClass = `panel panel--match${panelStyle ? ' panel--anchored' : ''}`
  const [match, setMatch] = useState<Match | null>(null)
  const [scoreReport, setScoreReport] = useState<ScoreReport | null>(null)
  const [dispute, setDispute] = useState<Dispute | null>(null)
  const [messages, setMessages] = useState<MatchMessage[]>([])
  const [handles, setHandles] = useState<Record<string, string>>({})
  const [elos, setElos] = useState<Record<string, number>>({})
  const [draft, setDraft] = useState('')
  const [setInputs, setSetInputs] = useState<SetInput[]>(DEFAULT_SET_INPUTS)
  const [disputeReason, setDisputeReason] = useState('')
  const [showDispute, setShowDispute] = useState(false)
  const [showAmendForm, setShowAmendForm] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [confirmCancel, setConfirmCancel] = useState(false)
  const [showRules, setShowRules] = useState(false)
  const [nowMs, setNowMs] = useState(0)
  const chatEndRef = useRef<HTMLDivElement>(null)
  const scrollChatToEnd = useCallback(() => {
    requestAnimationFrame(() => {
      chatEndRef.current?.scrollIntoView({ behavior: 'smooth' })
    })
  }, [])

  const reload = useCallback(async () => {
    let m = await fetchMatch(matchId)
    if (!m) throw new Error('Match not found')
    m = await autoConfirmScoreIfDue(m)
    setMatch(m)
    const participant = m.participants.includes(currentUserId)
    const reports = await fetchScoreReports(matchId)
    setScoreReport(reports[0] ?? null)
    const canViewPrivate = participant || isAdmin
    setDispute(canViewPrivate ? await fetchDispute(matchId) : null)
    setMessages(canViewPrivate ? await fetchMatchMessages(matchId) : [])
    const ids = [m.defenderUserId, m.challengerUserId]
    const h = await fetchHandles(ids)
    setHandles(h)
    const profiles = await Promise.all(ids.map((id) => fetchProfileByUserId(id)))
    setElos(
      Object.fromEntries(
        profiles
          .filter((p): p is NonNullable<typeof p> => p !== null)
          .map((p) => [p.userId, p.globalElo ?? 1200]),
      ),
    )
    setNowMs(Date.now())
  }, [matchId, currentUserId, isAdmin])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void reload().catch((e: unknown) =>
      setError(e instanceof Error ? e.message : 'Failed to load match'),
    )
  }, [reload])

  useEffect(() => {
    const timer = setInterval(() => {
      void reload().catch(() => {})
    }, 8000)
    return () => clearInterval(timer)
  }, [reload])

  const act = async (fn: () => Promise<void>) => {
    setBusy(true)
    setError(null)
    try {
      await fn()
      await reload()
      onChanged()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Action failed')
    } finally {
      setBusy(false)
    }
  }

  if (!match && !error) {
    return (
      <aside className={panelClass} style={panelStyle}>
        <p className="panel__empty">Loading match…</p>
      </aside>
    )
  }

  if (!match) {
    return (
      <aside className={panelClass} style={panelStyle}>
        <div className="panel__header">
          <h2>Match</h2>
          <button type="button" className="btn btn--icon" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>
        <p className="form-error">{error}</p>
      </aside>
    )
  }

  const court = courts[match.courtId]
  const isParticipant = match.participants.includes(currentUserId)
  const canCancel = isParticipant && match.status === 'CONTRACTED'
  const postWindowOpen = canPostScore(match, nowMs)
  const report = scoreReport
  const isWinner = Boolean(report && report.claimedWinnerUserId === currentUserId)
  const isLoser = Boolean(report && report.claimedWinnerUserId !== currentUserId && isParticipant)
  const confirmOpen =
    match.status === 'REPORTED' &&
    match.scoreReportedAt &&
    nowMs <= scoreConfirmDeadline(match.scoreReportedAt).getTime()

  const updateSet = (index: number, patch: Partial<SetInput>) => {
    setSetInputs((prev) => prev.map((s, i) => (i === index ? { ...s, ...patch } : s)))
  }

  const scoreForm = (onSubmit: () => Promise<void>, submitLabel: string) => (
    <form
      className="gauntlet-form"
      onSubmit={(e) => {
        e.preventDefault()
        void act(onSubmit)
      }}
    >
      {[0, 1, 2].map((i) => (
        <div key={i} className="score-set-block">
          <div className="score-set-block__label">Set {i + 1}</div>
          <div className="score-set-row">
            <label>
              You (games)
              <input
                type="number"
                min={0}
                max={7}
                value={setInputs[i].mine}
                onChange={(e) => updateSet(i, { mine: e.target.value })}
                placeholder={i === 2 ? '—' : ''}
              />
            </label>
            <label>
              Opponent (games)
              <input
                type="number"
                min={0}
                max={7}
                value={setInputs[i].theirs}
                onChange={(e) => updateSet(i, { theirs: e.target.value })}
                placeholder={i === 2 ? '—' : ''}
              />
            </label>
          </div>
          {needsTiebreakInput(setInputs[i].mine, setInputs[i].theirs) && (
            <div className="score-set-row score-set-row--tb">
              <label>
                Loser&apos;s tiebreak points
                <input
                  type="number"
                  min={0}
                  max={20}
                  value={setInputs[i].tbLoser}
                  onChange={(e) => updateSet(i, { tbLoser: e.target.value })}
                  placeholder="5"
                />
              </label>
            </div>
          )}
        </div>
      ))}
      <button type="submit" className="btn btn--primary" disabled={busy}>
        {busy ? 'Saving…' : submitLabel}
      </button>
    </form>
  )

  const playerCard = (userId: string, role: string) => (
    <div className={`matchup-card${userId === currentUserId ? ' matchup-card--you' : ''}`}>
      <div className="matchup-card__role">{role}</div>
      <button type="button" className="name-link matchup-card__name" onClick={() => onViewProfile(userId)}>
        {handles[userId] ?? 'Unknown player'}
      </button>
      <div className="matchup-card__elo">{elos[userId] ?? 1200} Elo</div>
    </div>
  )

  return (
    <aside className={panelClass} style={panelStyle}>
      <div className="panel__header">
        <div>
          <h2>{court?.name ?? 'Match'}</h2>
          <p className="panel__meta">{formatDateTime(match.scheduledAt)}</p>
        </div>
        <div className="panel__header-actions">
          <RulesButton onClick={() => setShowRules(true)} />
          {(match.status === 'CONTRACTED' || match.status === 'LIVE') && court && (
            <AddToCalendarButton
              url={googleCalendarUrlForMatch(match, court, handles)}
              compact
            />
          )}
          <button type="button" className="btn btn--icon" onClick={onClose} aria-label="Close">
            ×
          </button>
        </div>
      </div>

      <MatchRulesPopover
        open={showRules}
        onClose={() => setShowRules(false)}
        stakes={match.stakes}
        format={match.format}
        showAfterPlay={match.status === 'CONTRACTED'}
      />

      <div className="matchup">
        {playerCard(match.defenderUserId, 'Host')}
        <div className="matchup__vs">vs</div>
        {playerCard(match.challengerUserId, 'Requester')}
      </div>

      {match.status === 'CONTRACTED' && (
        <p className="panel__meta panel__meta--compact">
          Winner posts score within 24 hours after play.
        </p>
      )}

      {postWindowOpen && isParticipant && !report && (
        <section className="panel__section score-section">
          <h3>Post your score</h3>
          <p className="panel__meta">
            Deadline: {scorePostDeadline(match.scheduledAt).toLocaleString()}
          </p>
          {scoreForm(async () => {
            const isDefender = match.defenderUserId === currentUserId
            const { scores, error: buildError } = buildSetScores(setInputs, isDefender)
            if (buildError) throw new Error(buildError)
            await submitScoreReport({ match, reporterUserId: currentUserId, setScores: scores })
          }, 'Post score (I won)')}
        </section>
      )}

      {report && match.status === 'REPORTED' && (
        <section className="panel__section score-section">
          <h3>Score posted</h3>
          <p className="panel__meta">
            {handles[report.claimedWinnerUserId] ?? 'Winner'} claims{' '}
            {formatAllSets(report.setScores, match, currentUserId)}
          </p>
          {match.scoreReportedAt && confirmOpen && (
            <p className="panel__meta">
              {isLoser
                ? `Confirm or dispute by ${scoreConfirmDeadline(match.scoreReportedAt).toLocaleString()}`
                : `Waiting for opponent — auto-confirms ${scoreConfirmDeadline(match.scoreReportedAt).toLocaleString()}`}
            </p>
          )}
          {isLoser && confirmOpen && !showDispute && (
            <div className="gauntlet-form__actions">
              <button
                type="button"
                className="btn btn--primary"
                disabled={busy}
                onClick={() =>
                  void act(async () => {
                    await confirmPostedScore(match, report, currentUserId)
                  })
                }
              >
                {busy ? 'Confirming…' : 'Confirm score'}
              </button>
              <button type="button" className="btn btn--ghost" disabled={busy} onClick={() => setShowDispute(true)}>
                Dispute
              </button>
            </div>
          )}
          {isLoser && showDispute && (
            <form
              className="gauntlet-form"
              onSubmit={(e) => {
                e.preventDefault()
                void act(async () => {
                  await disputePostedScore(match, report, currentUserId, disputeReason)
                  setShowDispute(false)
                })
              }}
            >
              <label>
                Why is this wrong?
                <input value={disputeReason} onChange={(e) => setDisputeReason(e.target.value)} required />
              </label>
              <div className="gauntlet-form__actions">
                <button type="submit" className="btn btn--danger btn--small" disabled={busy}>
                  Submit dispute
                </button>
                <button type="button" className="btn btn--ghost btn--small" onClick={() => setShowDispute(false)}>
                  Cancel
                </button>
              </div>
            </form>
          )}
        </section>
      )}

      {match.status === 'FINAL' && match.finalSetScores && (
        <div className="match-notice match-notice--live">
          Final: {formatAllSets(match.finalSetScores, match, currentUserId)}
          {match.winnerUserId && (
            <> — {handles[match.winnerUserId] ?? 'Winner'} wins</>
          )}
        </div>
      )}

      {match.status === 'DISPUTED' && report && dispute && (
        <section className="panel__section score-section">
          <h3>Score disputed</h3>
          <p className="panel__meta">
            {handles[report.claimedWinnerUserId] ?? 'Winner'} posted{' '}
            {formatAllSets(report.setScores, match, currentUserId)}
          </p>
          <p className="panel__meta">
            {handles[dispute.raisedByUserId] ?? 'Opponent'}: &ldquo;{dispute.reason}&rdquo;
          </p>
          {dispute.status === 'AWAITING_ADMIN' && (
            <p className="panel__empty">
              Winner stood by their score — an admin will review and resolve this match.
            </p>
          )}
          {dispute.status === 'OPEN' && isWinner && !showAmendForm && (
            <div className="gauntlet-form__actions">
              <button
                type="button"
                className="btn btn--primary"
                disabled={busy}
                onClick={() => {
                  const isDefender = match.defenderUserId === currentUserId
                  const scores = parseSetScores(report.setScores)
                  setSetInputs(setScoresToSetInputs(scores, isDefender))
                  setShowAmendForm(true)
                }}
              >
                Amend score
              </button>
              <button
                type="button"
                className="btn btn--ghost"
                disabled={busy}
                onClick={() =>
                  void act(async () => {
                    await doubleDownOnDisputedScore({
                      match,
                      report,
                      dispute,
                      winnerUserId: currentUserId,
                    })
                  })
                }
              >
                {busy ? 'Submitting…' : 'Stand by score (admin review)'}
              </button>
            </div>
          )}
          {dispute.status === 'OPEN' && isWinner && showAmendForm && (
            <>
              <p className="panel__meta">Update the score — your opponent must confirm again.</p>
              {scoreForm(async () => {
                const isDefender = match.defenderUserId === currentUserId
                const { scores, error: buildError } = buildSetScores(setInputs, isDefender)
                if (buildError) throw new Error(buildError)
                await amendDisputedScore({
                  match,
                  report,
                  dispute,
                  winnerUserId: currentUserId,
                  setScores: scores,
                })
                setShowAmendForm(false)
              }, 'Submit amended score')}
              <button
                type="button"
                className="btn btn--ghost btn--small"
                disabled={busy}
                onClick={() => setShowAmendForm(false)}
              >
                Cancel
              </button>
            </>
          )}
          {dispute.status === 'OPEN' && !isWinner && (
            <p className="panel__meta">Waiting for the winner to amend their score or escalate to admin.</p>
          )}
        </section>
      )}

      {isParticipant && (
      <section className="match-chat">
        <h3>Messages</h3>
        <div className="match-chat__log">
          {messages.length === 0 && (
            <p className="panel__empty">No messages yet. Coordinate here.</p>
          )}
          {messages.map((msg) => {
            const mine = msg.senderUserId === currentUserId
            return (
              <div
                key={msg.id}
                className={`match-chat__bubble${mine ? ' match-chat__bubble--mine' : ''}`}
              >
                <div className="match-chat__sender">
                  {mine ? 'You' : handles[msg.senderUserId] ?? 'Them'}
                </div>
                <div>{msg.body}</div>
                <div className="match-chat__time">
                  {new Date(msg.sentAt).toLocaleTimeString(undefined, {
                    hour: 'numeric',
                    minute: '2-digit',
                  })}
                </div>
              </div>
            )
          })}
          <div ref={chatEndRef} />
        </div>
        {['CONTRACTED', 'REPORTED', 'DISPUTED'].includes(match.status) && (
          <form
            className="match-chat__compose"
            onSubmit={(e) => {
              e.preventDefault()
              const body = draft.trim()
              if (!body) return
              void act(async () => {
                await sendMatchMessage({ match, senderUserId: currentUserId, body })
                setDraft('')
                scrollChatToEnd()
              })
            }}
          >
            <input
              type="text"
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              placeholder="Running late, on my way…"
              disabled={busy}
            />
            <button type="submit" className="btn btn--primary btn--small" disabled={busy || !draft.trim()}>
              Send
            </button>
          </form>
        )}
      </section>
      )}

      {error && <p className="form-error">{error}</p>}

      {canCancel && (
        <div className="match-actions">
          {!confirmCancel ? (
            <button type="button" className="btn btn--ghost" disabled={busy} onClick={() => setConfirmCancel(true)}>
              Cancel match
            </button>
          ) : (
            <div className="match-cancel-confirm">
              <p>Cancel this match? The posted challenge will reopen if possible.</p>
              <div className="gauntlet-form__actions">
                <button
                  type="button"
                  className="btn btn--danger"
                  disabled={busy}
                  onClick={() =>
                    void act(async () => {
                      await cancelMatch(match, currentUserId)
                      onClose()
                    })
                  }
                >
                  {busy ? 'Cancelling…' : 'Yes, cancel'}
                </button>
                <button type="button" className="btn btn--ghost" onClick={() => setConfirmCancel(false)} disabled={busy}>
                  Never mind
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {match.status === 'CANCELLED' && (
        <p className="panel__empty">This match was cancelled.</p>
      )}

      {isAdmin && match && (
        <AdminMatchTools
          match={match}
          dispute={dispute}
          report={scoreReport}
          handles={handles}
          adminUserId={currentUserId}
          onResolved={() => {
            void reload().then(() => onChanged())
          }}
          onDeleted={() => {
            onChanged()
            onClose()
          }}
        />
      )}
    </aside>
  )
}
