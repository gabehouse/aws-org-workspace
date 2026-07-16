import { useEffect, useState } from 'react'
import { adminUpdatePlayerStats, type PlayerProfile } from '../lib/data'

interface AdminProfileToolsProps {
  profile: PlayerProfile
  onUpdated: (profile: PlayerProfile) => void
}

export function AdminProfileTools({ profile, onUpdated }: AdminProfileToolsProps) {
  const [globalElo, setGlobalElo] = useState(String(profile.globalElo ?? 1200))
  const [wins, setWins] = useState(String(profile.wins ?? 0))
  const [losses, setLosses] = useState(String(profile.losses ?? 0))
  const [matchesCompleted, setMatchesCompleted] = useState(String(profile.matchesCompleted ?? 0))
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setGlobalElo(String(profile.globalElo ?? 1200))
    setWins(String(profile.wins ?? 0))
    setLosses(String(profile.losses ?? 0))
    setMatchesCompleted(String(profile.matchesCompleted ?? 0))
  }, [profile])

  const parseIntField = (label: string, raw: string): number => {
    const n = Number(raw)
    if (!Number.isInteger(n) || n < 0) throw new Error(`Invalid ${label}`)
    return n
  }

  return (
    <section className="panel__section panel__section--admin">
      <h3>Admin — override stats</h3>
      <form
        className="gauntlet-form"
        onSubmit={(e) => {
          e.preventDefault()
          setBusy(true)
          setError(null)
          void (async () => {
            try {
              const updated = await adminUpdatePlayerStats({
                profile,
                globalElo: parseIntField('Elo', globalElo),
                wins: parseIntField('wins', wins),
                losses: parseIntField('losses', losses),
                matchesCompleted: parseIntField('matches completed', matchesCompleted),
              })
              onUpdated(updated)
            } catch (err) {
              setError(err instanceof Error ? err.message : 'Update failed')
            } finally {
              setBusy(false)
            }
          })()
        }}
      >
        <label>
          Global Elo
          <input
            type="number"
            min={0}
            step={1}
            value={globalElo}
            onChange={(e) => setGlobalElo(e.target.value)}
            required
          />
        </label>
        <label>
          Wins
          <input
            type="number"
            min={0}
            step={1}
            value={wins}
            onChange={(e) => setWins(e.target.value)}
            required
          />
        </label>
        <label>
          Losses
          <input
            type="number"
            min={0}
            step={1}
            value={losses}
            onChange={(e) => setLosses(e.target.value)}
            required
          />
        </label>
        <label>
          Matches completed
          <input
            type="number"
            min={0}
            step={1}
            value={matchesCompleted}
            onChange={(e) => setMatchesCompleted(e.target.value)}
            required
          />
        </label>
        <div className="gauntlet-form__actions">
          <button type="submit" className="btn btn--primary btn--small" disabled={busy}>
            {busy ? 'Saving…' : 'Save stats'}
          </button>
        </div>
        {error && <p className="form-error">{error}</p>}
      </form>
    </section>
  )
}
