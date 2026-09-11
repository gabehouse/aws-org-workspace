import { useEffect, useState } from 'react'
import { adminUpdatePlayerStats, type PlayerProfile } from '../lib/data'
import { celoToElo, CELO_MAX, CELO_MIN, eloToCelo } from '../lib/celoDisplay'

interface AdminProfileToolsProps {
  profile: PlayerProfile
  onUpdated: (profile: PlayerProfile) => void
}

export function AdminProfileTools({ profile, onUpdated }: AdminProfileToolsProps) {
  const [displayCelo, setDisplayCelo] = useState(() =>
    eloToCelo(profile.globalElo ?? 1200).toFixed(1),
  )
  const [wins, setWins] = useState(String(profile.wins ?? 0))
  const [losses, setLosses] = useState(String(profile.losses ?? 0))
  const [matchesCompleted, setMatchesCompleted] = useState(String(profile.matchesCompleted ?? 0))
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setDisplayCelo(eloToCelo(profile.globalElo ?? 1200).toFixed(1))
    setWins(String(profile.wins ?? 0))
    setLosses(String(profile.losses ?? 0))
    setMatchesCompleted(String(profile.matchesCompleted ?? 0))
  }, [profile])

  const parseIntField = (label: string, raw: string): number => {
    const n = Number(raw)
    if (!Number.isInteger(n) || n < 0) throw new Error(`Invalid ${label}`)
    return n
  }

  const parseCeloField = (raw: string): number => {
    const n = Number(raw)
    if (!Number.isFinite(n) || n < CELO_MIN || n > CELO_MAX) {
      throw new Error(`Celo must be between ${CELO_MIN} and ${CELO_MAX}`)
    }
    return celoToElo(Math.round(n * 10) / 10)
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
                globalElo: parseCeloField(displayCelo),
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
          Celo (display rating)
          <input
            type="number"
            min={CELO_MIN}
            max={CELO_MAX}
            step={0.1}
            value={displayCelo}
            onChange={(e) => setDisplayCelo(e.target.value)}
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
