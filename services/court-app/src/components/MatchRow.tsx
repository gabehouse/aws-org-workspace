import type { Dispute, Match } from '../lib/data'
import { formatDateTime, matchDisplayForViewer } from '../lib/data'
import { formatAllSets } from '../lib/scores'

const CANCELLED_STATUSES = new Set(['CANCELLED', 'VOIDED', 'RAINED_OUT'])

function formatMatchDate(iso: string): string {
  return new Date(iso).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

function formatScore(match: Match, userId: string): string | null {
  const line = formatAllSets(match.finalSetScores, match, userId)
  return line || null
}

interface MatchRowProps {
  match: Match
  handles: Record<string, string>
  isNew?: boolean
  /** Profile history: show vs opponent from this user's perspective */
  viewerUserId?: string
  dispute?: Dispute | null
  /** Court panel: show both players */
  layout?: 'profile' | 'court'
  onOpenMatch?: (matchId: string) => void
  onViewProfile?: (userId: string) => void
}

export function MatchRow({
  match,
  handles,
  isNew,
  viewerUserId,
  dispute,
  layout = 'profile',
  onOpenMatch,
  onViewProfile,
}: MatchRowProps) {
  const cancelled = CANCELLED_STATUSES.has(match.status)
  const openable = Boolean(onOpenMatch)
  const display = matchDisplayForViewer(match, viewerUserId, {
    disputeStatus: dispute?.status,
  })

  const nameLink = (userId: string, label: string) =>
    onViewProfile ? (
      <button
        type="button"
        className="name-link"
        onClick={(e) => {
          e.stopPropagation()
          onViewProfile(userId)
        }}
      >
        {label}
      </button>
    ) : (
      label
    )

  const opponentId =
    viewerUserId && match.defenderUserId === viewerUserId
      ? match.challengerUserId
      : viewerUserId
        ? match.defenderUserId
        : null
  const score = viewerUserId ? formatScore(match, viewerUserId) : null

  const titleLine =
    layout === 'court' ? (
      <>
        {nameLink(match.defenderUserId, handles[match.defenderUserId] ?? 'Unknown')}{' '}
        <span className="match-row__vs">vs</span>{' '}
        {nameLink(match.challengerUserId, handles[match.challengerUserId] ?? 'Unknown')}
      </>
    ) : (
      <>
        vs {nameLink(opponentId!, handles[opponentId!] ?? 'Unknown player')}
      </>
    )

  const statusMeta =
    display.actionNotice ??
    (cancelled || display.showStatusInMeta ? display.badgeTitle : null)

  const metaParts = [
    layout === 'court' ? formatDateTime(match.scheduledAt) : formatMatchDate(match.scheduledAt),
    score,
    statusMeta,
  ].filter(Boolean)

  return (
    <div
      className={`match-row${openable ? ' match-row--clickable' : ''}${cancelled ? ' match-row--cancelled' : ''}${display.needsAction ? ' match-row--action' : ''}${isNew ? ' match-row--new' : ''}`}
      role={openable ? 'button' : undefined}
      tabIndex={openable ? 0 : undefined}
      onClick={openable ? () => onOpenMatch!(match.id) : undefined}
      onKeyDown={
        openable
          ? (e) => {
              if (e.key === 'Enter' || e.key === ' ') onOpenMatch!(match.id)
            }
          : undefined
      }
    >
      <span
        className={`match-row__outcome match-row__outcome--${display.badgeVariant}`}
        title={display.badgeTitle}
      >
        {display.badge}
      </span>
      <div className="match-row__body">
        <div className={cancelled ? 'match-row__title match-row__title--struck' : 'match-row__title'}>
          {titleLine}
          {isNew && <span className="match-row__new">New</span>}
        </div>
        <div
          className={`match-row__meta${cancelled ? ' match-row__meta--cancelled' : ''}${display.needsAction ? ' match-row__meta--action' : ''}`}
        >
          {metaParts.join(' · ')}
        </div>
      </div>
    </div>
  )
}
