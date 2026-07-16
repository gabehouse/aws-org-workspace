import { useState } from 'react'
import { adminDeleteCourt, type Court } from '../lib/data'

interface AdminCourtToolsProps {
  court: Court
  onDeleted: () => void
}

export function AdminCourtTools({ court, onDeleted }: AdminCourtToolsProps) {
  const [confirmDelete, setConfirmDelete] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

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
      <p className="panel__meta">
        Remove this court and all gauntlets, requests, and matches tied to it.
      </p>

      {!confirmDelete ? (
        <button
          type="button"
          className="btn btn--ghost btn--small admin-danger-btn"
          disabled={busy}
          onClick={() => setConfirmDelete(true)}
        >
          Delete court
        </button>
      ) : (
        <div className="gauntlet-form__actions">
          <p className="panel__meta">
            Permanently delete <strong>{court.name}</strong> and all related records?
          </p>
          <button
            type="button"
            className="btn btn--primary btn--small admin-danger-btn"
            disabled={busy}
            onClick={() =>
              void act(async () => {
                await adminDeleteCourt(court.id)
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
