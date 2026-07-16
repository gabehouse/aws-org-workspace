import type { Dispute, Match, PlayerProfile } from '../lib/data'
import { MatchRow } from './MatchRow'

function completionRate(matches: Match[] | null): string {
  if (!matches?.length) return '—'
  const decided = matches.filter((m) =>
    ['FINAL', 'CANCELLED', 'VOIDED', 'RAINED_OUT'].includes(m.status),
  )
  if (!decided.length) return '—'
  const finals = decided.filter((m) => m.status === 'FINAL').length
  return `${Math.round((finals / decided.length) * 100)}%`
}

interface ProfileCardProps {
  profile: PlayerProfile
  /** null while loading */
  matches: Match[] | null
  disputes?: Record<string, Dispute | null>
  opponentHandles: Record<string, string>
  onViewProfile?: (userId: string) => void
  onOpenMatch?: (matchId: string) => void
  /** When false, hero + stats only (match history rendered separately). */
  showMatchHistory?: boolean
}

export function ProfileMatchHistory({
  matches,
  disputes = {},
  opponentHandles,
  profile,
  onViewProfile,
  onOpenMatch,
}: Pick<
  ProfileCardProps,
  'matches' | 'disputes' | 'opponentHandles' | 'profile' | 'onViewProfile' | 'onOpenMatch'
>) {
  return (
    <>
      <h3 className="profile-card__section-title">Match history</h3>
      {matches === null && <p className="panel__empty">Loading…</p>}
      {matches?.length === 0 && <p className="panel__empty">No matches yet.</p>}
      {matches?.map((m) => (
        <MatchRow
          key={m.id}
          match={m}
          handles={opponentHandles}
          viewerUserId={profile.userId}
          dispute={disputes[m.id]}
          onOpenMatch={onOpenMatch}
          onViewProfile={onViewProfile}
        />
      ))}
    </>
  )
}

export function ProfileCard({
  profile,
  matches,
  disputes = {},
  opponentHandles,
  onViewProfile,
  onOpenMatch,
  showMatchHistory = true,
}: ProfileCardProps) {
  return (
    <div className="profile-card">
      <div className="profile-card__hero">
        <div>
          <div className="profile-card__handle">{profile.handle}</div>
          {profile.bio && <div className="panel__meta">{profile.bio}</div>}
        </div>
      </div>

      <div className="profile-card__stats">
        <div className="stat-tile">
          <strong>{profile.globalElo ?? 1200}</strong>
          <span>Elo</span>
        </div>
        <div className="stat-tile">
          <strong>
            {profile.wins ?? 0}–{profile.losses ?? 0}
          </strong>
          <span>Record</span>
        </div>
        <div className="stat-tile">
          <strong>{completionRate(matches)}</strong>
          <span>Completion</span>
        </div>
      </div>

      {showMatchHistory && (
        <ProfileMatchHistory
          matches={matches}
          disputes={disputes}
          opponentHandles={opponentHandles}
          profile={profile}
          onViewProfile={onViewProfile}
          onOpenMatch={onOpenMatch}
        />
      )}
    </div>
  )
}
