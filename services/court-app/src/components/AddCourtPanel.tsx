import { useState } from 'react'
import { createCourt, type Court, type CourtSurface } from '../lib/data'

const SURFACES: CourtSurface[] = ['HARD', 'CLAY', 'GRASS', 'CARPET', 'OTHER']

interface AddCourtPanelProps {
  position: { lat: number; lng: number }
  onClose: () => void
  onCreated: (court: Court) => void
}

export function AddCourtPanel({ position, onClose, onCreated }: AddCourtPanelProps) {
  const [name, setName] = useState('')
  const [surface, setSurface] = useState<CourtSurface>('HARD')
  const [courtCount, setCourtCount] = useState(1)
  const [hasLights, setHasLights] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const submit = async () => {
    setSubmitting(true)
    setError(null)
    try {
      onCreated(
        await createCourt({
          name: name.trim(),
          lat: position.lat,
          lng: position.lng,
          surface,
          courtCount,
          hasLights,
        }),
      )
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to add court')
      setSubmitting(false)
    }
  }

  return (
    <aside className="panel">
      <div className="panel__header">
        <h2>New court</h2>
        <button type="button" className="btn btn--icon" onClick={onClose} aria-label="Close">
          ×
        </button>
      </div>
      <p className="panel__meta">
        {position.lat.toFixed(5)}, {position.lng.toFixed(5)} — tap the map again to adjust
      </p>
      <form
        className="panel__section gauntlet-form"
        onSubmit={(e) => {
          e.preventDefault()
          void submit()
        }}
      >
        <label>
          Name
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. Riverside Park Courts"
            required
          />
        </label>
        <label>
          Surface
          <select
            value={surface}
            onChange={(e) => setSurface(e.target.value as CourtSurface)}
          >
            {SURFACES.map((s) => (
              <option key={s} value={s}>
                {s.charAt(0) + s.slice(1).toLowerCase()}
              </option>
            ))}
          </select>
        </label>
        <label>
          Number of courts
          <input
            type="number"
            min={1}
            max={50}
            value={courtCount}
            onChange={(e) => setCourtCount(Number(e.target.value))}
          />
        </label>
        <label className="gauntlet-form__checkbox">
          <input
            type="checkbox"
            checked={hasLights}
            onChange={(e) => setHasLights(e.target.checked)}
          />
          Has lights
        </label>
        {error && <p className="form-error">{error}</p>}
        <div className="gauntlet-form__actions">
          <button type="submit" className="btn btn--primary" disabled={submitting || !name.trim()}>
            {submitting ? 'Adding…' : 'Add court'}
          </button>
          <button type="button" className="btn btn--ghost" onClick={onClose} disabled={submitting}>
            Cancel
          </button>
        </div>
      </form>
    </aside>
  )
}
