import { useEffect, useState } from 'react'
import {
  fetchDispute,
  fetchHandles,
  fetchMatchHistory,
  fetchProfileByUserId,
  type Dispute,
  type Match,
  type PlayerProfile,
} from '../lib/data'
import { ProfileCard } from './ProfileCard'
import { AdminProfileTools } from './AdminProfileTools'

interface ProfilePanelProps {
  userId: string
  isAdmin?: boolean
  /** Pop-over on top of other panels (with backdrop handled by parent) */
  popup?: boolean
  onClose: () => void
  onViewProfile: (userId: string) => void
  onOpenMatch: (matchId: string) => void
}

/** Profile view for another player. */
export function ProfilePanel({
  userId,
  isAdmin = false,
  popup = false,
  onClose,
  onViewProfile,
  onOpenMatch,
}: ProfilePanelProps) {
  const [profile, setProfile] = useState<PlayerProfile | null | 'missing'>(null)
  const [matches, setMatches] = useState<Match[] | null>(null)
  const [disputes, setDisputes] = useState<Record<string, Dispute | null>>({})
  const [opponentHandles, setOpponentHandles] = useState<Record<string, string>>({})
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    void (async () => {
      try {
        const [p, history] = await Promise.all([
          fetchProfileByUserId(userId),
          fetchMatchHistory(userId),
        ])
        if (cancelled) return
        setProfile(p ?? 'missing')
        setMatches(history)
        const disputed = history.filter((m) => m.status === 'DISPUTED')
        if (disputed.length) {
          const pairs = await Promise.all(
            disputed.map(async (m) => [m.id, await fetchDispute(m.id)] as const),
          )
          if (!cancelled) setDisputes(Object.fromEntries(pairs))
        }
        const opponents = [
          ...new Set(
            history.map((m) =>
              m.defenderUserId === userId ? m.challengerUserId : m.defenderUserId,
            ),
          ),
        ]
        if (opponents.length) {
          const h = await fetchHandles(opponents)
          if (!cancelled) setOpponentHandles(h)
        }
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : 'Failed to load profile')
      }
    })()
    return () => {
      cancelled = true
    }
  }, [userId])

  return (
    <aside className={`panel panel--profile${popup ? ' panel--popup' : ''}`}>
      <div className="panel__header">
        <h2>Player profile</h2>
        <button type="button" className="btn btn--icon" onClick={onClose} aria-label="Close">
          ×
        </button>
      </div>

      {error && <p className="form-error">{error}</p>}
      {profile === null && !error && <p className="panel__empty">Loading…</p>}
      {profile === 'missing' && (
        <p className="panel__empty">This player hasn't set up a profile yet.</p>
      )}
      {profile && profile !== 'missing' && (
        <>
          <ProfileCard
            profile={profile}
            matches={matches}
            disputes={disputes}
            opponentHandles={opponentHandles}
            onViewProfile={onViewProfile}
            onOpenMatch={onOpenMatch}
          />
          {isAdmin && (
            <AdminProfileTools
              profile={profile}
              onUpdated={(updated) => setProfile(updated)}
            />
          )}
        </>
      )}
    </aside>
  )
}
