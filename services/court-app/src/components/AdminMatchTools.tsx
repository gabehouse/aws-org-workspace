import { useState } from 'react'
import {
  adminDeleteMatch,
  adminResolveDispute,
  type Dispute,
  type Match,
  type ScoreReport,
} from '../lib/data'

interface AdminMatchToolsProps {
  match: Match
  dispute: Dispute | null
  report: ScoreReport | null
  handles: Record<string, string>
  adminUserId: string
  onResolved: () => void
  onDeleted: () => void
}

export function AdminMatchTools({
  match,
  dispute,
  report,
  handles,
  adminUserId,
  onResolved,
  onDeleted,
}: AdminMatchToolsProps) {
  const [resolution, setResolution] = useState<'uphold' | 'overturn' | 'void'>('uphold')
  const [overturnWinnerUserId, setOverturnWinnerUserId] = useState(match.defenderUserId)
  const [resolutionNote, setResolutionNote] = useState('')
  const [confirmDelete, setConfirmDelete] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const disputeOpen =
    match.status === 'DISPUTED' &&
    dispute &&
    !['RESOLVED_UPHELD', 'RESOLVED_OVERTURNED', 'MATCH_VOIDED'].includes(dispute.status)

  const act = async (fn: () => Promise<void>) => {
    setBusy(true)
    setError(null)
    try {
      await fn()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Admin action failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="panel__section panel__section--admin">
      <h3>Admin</h3>

      {disputeOpen && (
        <form
          className="gauntlet-form"
          onSubmit={(e) => {
            e.preventDefault()
            void act(async () => {
              await adminResolveDispute({
                match,
                dispute: dispute!,
                report,
                adminUserId,
                resolution,
                overturnWinnerUserId:
                  resolution === 'overturn' ? overturnWinnerUserId : undefined,
                resolutionNote,
              })
              onResolved()
            })
          }}
        >
          <p className="panel__meta">Resolve this disputed match.</p>
          <label>
            Resolution
            <select
              value={resolution}
              onChange={(e) => setResolution(e.target.value as 'uphold' | 'overturn' | 'void')}
            >
              <option value="uphold">Uphold posted score</option>
              <option value="overturn">Overturn — pick winner</option>
              <option value="void">Void match</option>
            </select>
          </label>
          {resolution === 'overturn' && (
            <label>
              Winner
              <select
                value={overturnWinnerUserId}
                onChange={(e) => setOverturnWinnerUserId(e.target.value)}
              >
                <option value={match.defenderUserId}>
                  {handles[match.defenderUserId] ?? 'Defender'}
                </option>
                <option value={match.challengerUserId}>
                  {handles[match.challengerUserId] ?? 'Requester'}
                </option>
              </select>
            </label>
          )}
          <label>
            Note (optional)
            <input
              type="text"
              value={resolutionNote}
              onChange={(e) => setResolutionNote(e.target.value)}
              placeholder="Resolution notes for the record"
            />
          </label>
          <div className="gauntlet-form__actions">
            <button type="submit" className="btn btn--primary btn--small" disabled={busy}>
              {busy ? 'Saving…' : 'Resolve dispute'}
            </button>
          </div>
        </form>
      )}

      {!confirmDelete ? (
        <button
          type="button"
          className="btn btn--ghost btn--small admin-danger-btn"
          disabled={busy}
          onClick={() => setConfirmDelete(true)}
        >
          Delete match
        </button>
      ) : (
        <div className="gauntlet-form__actions">
          <p className="panel__meta">Permanently delete this match and related records?</p>
          <button
            type="button"
            className="btn btn--primary btn--small admin-danger-btn"
            disabled={busy}
            onClick={() =>
              void act(async () => {
                await adminDeleteMatch(match.id)
                onDeleted()
              })
            }
          >
            {busy ? 'Deleting…' : 'Confirm delete'}
          </button>
          <button
            type="button"
            className="btn btn--ghost btn--small"
            disabled={busy}
            onClick={() => setConfirmDelete(false)}
          >
            Cancel
          </button>
        </div>
      )}

      {error && <p className="form-error">{error}</p>}
    </section>
  )
}
