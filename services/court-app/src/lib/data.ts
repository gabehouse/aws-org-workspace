import { generateClient } from 'aws-amplify/data'
import ngeohash from 'ngeohash'
import type { Schema } from '../../amplify/data/resource'
import { generateHandle, validateHandle } from './handles'
import { toAwsJson, type SetScore } from './scores'

export const client = generateClient<Schema>()

export type Court = Schema['Court']['type']
export type Gauntlet = Schema['Gauntlet']['type']
export type Challenge = Schema['Challenge']['type']
export type Match = Schema['Match']['type']
export type MatchMessage = Schema['MatchMessage']['type']
export type ScoreReport = Schema['ScoreReport']['type']
export type Dispute = Schema['Dispute']['type']
export type PlayerProfile = Schema['PlayerProfile']['type']
export type CourtSurface = NonNullable<Schema['Court']['type']['surface']>

export const GEOHASH_PRECISION = 5

export const DEFAULT_STAKES =
  'Both players bring a new, unopened tin of ITF-approved extra-duty (XD) balls. Open one tin to play the match; the winner takes home the remaining unopened tin.'
export { DEFAULT_FORMAT } from './scores'
export type { SetScore } from './scores'
export {
  buildSetScores,
  formatAllSets,
  parseSetScores,
  setScoresToSetInputs,
  toAwsJson,
} from './scores'

/**
 * Courts are a small static dataset, so we load them all and keep every pin
 * visible at any zoom. The `geohash` field is still written on create and its
 * index remains for viewport-scoped queries if the dataset ever outgrows this.
 */
export async function fetchAllCourts(): Promise<Court[]> {
  const all: Court[] = []
  let nextToken: string | null | undefined
  do {
    const { data, errors, nextToken: token } = await client.models.Court.list({
      limit: 500,
      nextToken,
    })
    if (errors?.length) throw new Error(errors[0].message)
    all.push(...data)
    nextToken = token
  } while (nextToken && all.length < 5000)
  return all
}

/** All ACTIVE gauntlets (open "I'm accepting matches" flags on the map). */
export async function fetchActiveGauntlets(): Promise<Gauntlet[]> {
  const all: Gauntlet[] = []
  let nextToken: string | null | undefined
  do {
    const { data, errors, nextToken: token } =
      await client.models.Gauntlet.listGauntletByStatus(
        { status: 'ACTIVE' },
        { limit: 200, nextToken },
      )
    if (errors?.length) throw new Error(errors[0].message)
    all.push(...data)
    nextToken = token
  } while (nextToken && all.length < 1000)
  return all
}

/** Best-effort handle lookup for gauntlet owners; unknown ids are omitted. */
export async function fetchHandles(
  userIds: string[],
): Promise<Record<string, string>> {
  const pairs = await Promise.all(
    userIds.map(async (userId) => {
      try {
        const { data } = await client.models.PlayerProfile.listPlayerProfileByUserId(
          { userId },
          { limit: 1 },
        )
        return [userId, data[0]?.handle] as const
      } catch {
        return [userId, undefined] as const
      }
    }),
  )
  return Object.fromEntries(
    pairs.filter((p): p is [string, string] => Boolean(p[1])),
  )
}

export async function createCourt(input: {
  name: string
  lat: number
  lng: number
  surface?: CourtSurface
  courtCount?: number
  hasLights?: boolean
  address?: string
}): Promise<Court> {
  const { data, errors } = await client.models.Court.create({
    ...input,
    geohash: ngeohash.encode(input.lat, input.lng, GEOHASH_PRECISION),
    isPublic: true,
  })
  if (errors?.length || !data) {
    throw new Error(errors?.[0]?.message ?? 'Court creation failed')
  }
  return data
}

export async function createGauntlet(input: {
  ownerUserId: string
  courtId: string
  stakes: string
  format: string
  note?: string
}): Promise<Gauntlet> {
  const { data, errors } = await client.models.Gauntlet.create({
    ...input,
    status: 'ACTIVE',
  })
  if (errors?.length || !data) {
    throw new Error(errors?.[0]?.message ?? 'Gauntlet creation failed')
  }
  return data
}

/** Pull your open challenge off the court. */
export async function withdrawGauntlet(
  gauntlet: Gauntlet,
  ownerUserId: string,
): Promise<void> {
  if (gauntlet.ownerUserId !== ownerUserId) {
    throw new Error('Only the poster can remove this challenge')
  }
  if (gauntlet.status !== 'ACTIVE') {
    throw new Error('This challenge is no longer active')
  }
  const { errors } = await client.models.Gauntlet.update({
    id: gauntlet.id,
    status: 'WITHDRAWN',
  })
  if (errors?.length) throw new Error(errors[0].message)
}

// ------------------------------------------------------------- negotiation

/** Challenger opens negotiation by proposing the first time. */
export async function createChallenge(input: {
  gauntlet: Gauntlet
  challengerUserId: string
  proposedStart: string
  message?: string
}): Promise<Challenge> {
  const { gauntlet, challengerUserId, proposedStart, message } = input
  const { data, errors } = await client.models.Challenge.create({
    gauntletId: gauntlet.id,
    challengerUserId,
    defenderUserId: gauntlet.ownerUserId,
    participants: [challengerUserId, gauntlet.ownerUserId],
    status: 'PENDING',
    proposedStart,
    proposedByUserId: challengerUserId,
    message,
  })
  if (errors?.length || !data) {
    throw new Error(errors?.[0]?.message ?? 'Challenge creation failed')
  }
  return data
}

/** Everything you're negotiating, as challenger or defender. */
export async function fetchMyChallenges(userId: string): Promise<Challenge[]> {
  const [asChallenger, asDefender] = await Promise.all([
    client.models.Challenge.listChallengeByChallengerUserId({
      challengerUserId: userId,
    }),
    client.models.Challenge.listChallengeByDefenderUserId({
      defenderUserId: userId,
    }),
  ])
  const errors = asChallenger.errors ?? asDefender.errors
  if (errors?.length) throw new Error(errors[0].message)
  const merged = [...asChallenger.data, ...asDefender.data]
  const byId = new Map(merged.map((c) => [c.id, c]))
  const unique = [...byId.values()]
  return Promise.all(
    unique.map((c) => (c.status === 'ACCEPTED' ? reconcileAcceptedChallenge(c) : c)),
  )
}

/** Counter with a new time; the turn passes to the other player. */
export async function nudgeChallenge(
  challenge: Challenge,
  userId: string,
  newStart: string,
): Promise<void> {
  const { errors } = await client.models.Challenge.update({
    id: challenge.id,
    proposedStart: newStart,
    proposedByUserId: userId,
    status: 'COUNTERED',
  })
  if (errors?.length) throw new Error(errors[0].message)
}

export const ACCEPT_MIN_HOURS_BEFORE = 2

/** True when the proposed start is at least 2 hours from now. */
export function canAcceptChallenge(challenge: Challenge, now = Date.now()): boolean {
  const start = new Date(challenge.proposedStart).getTime()
  return start - now >= ACCEPT_MIN_HOURS_BEFORE * 3_600_000
}

/**
 * Accept the time on the table: challenge -> ACCEPTED, a CONTRACTED Match is
 * created at that time, and the gauntlet is locked (best-effort: only the
 * gauntlet owner may write it, so the challenger's lock attempt is skipped —
 * the finalizeMatch Lambda will own these transitions later).
 */
export async function acceptChallenge(
  challenge: Challenge,
  _accepterUserId: string,
): Promise<Match> {
  if (!canAcceptChallenge(challenge)) {
    throw new Error(
      'This time is less than 2 hours away — nudge a new time or decline',
    )
  }

  const { data: gauntlet } = await client.models.Gauntlet.get({
    id: challenge.gauntletId,
  })
  if (!gauntlet) throw new Error('Gauntlet not found')

  const { errors: updateErrors } = await client.models.Challenge.update({
    id: challenge.id,
    status: 'ACCEPTED',
  })
  if (updateErrors?.length) throw new Error(updateErrors[0].message)

  const { data: match, errors: matchErrors } = await client.models.Match.create({
    gauntletId: challenge.gauntletId,
    challengeId: challenge.id,
    courtId: gauntlet.courtId,
    defenderUserId: challenge.defenderUserId,
    challengerUserId: challenge.challengerUserId,
    participants: [challenge.challengerUserId, challenge.defenderUserId],
    status: 'CONTRACTED',
    scheduledAt: challenge.proposedStart,
    stakes: gauntlet.stakes,
    format: gauntlet.format,
  })
  if (matchErrors?.length) throw new Error(matchErrors[0].message)
  if (!match) throw new Error('Match not created')

  try {
    await client.models.Gauntlet.update({ id: gauntlet.id, status: 'LOCKED' })
  } catch {
    // challenger isn't the gauntlet owner; lock is deferred
  }

  return match
}

/** Email the other player that their request was accepted, with a calendar link. */
export async function notifyMatchLocked(params: {
  recipientUserId: string
  accepterHandle: string
  courtName: string
  scheduledAt: string
  calendarUrl: string
  format?: string | null
  stakes?: string | null
}): Promise<void> {
  const { errors } = await client.mutations.notifyMatchLocked({
    recipientUserId: params.recipientUserId,
    accepterHandle: params.accepterHandle,
    courtName: params.courtName,
    scheduledAt: params.scheduledAt,
    calendarUrl: params.calendarUrl,
    format: params.format ?? undefined,
    stakes: params.stakes ?? undefined,
  })
  if (errors?.length) throw new Error(errors[0].message)
}

export async function closeChallenge(
  challenge: Challenge,
  status: 'DECLINED' | 'WITHDRAWN',
): Promise<void> {
  const { errors } = await client.models.Challenge.update({
    id: challenge.id,
    status,
  })
  if (errors?.length) throw new Error(errors[0].message)
}

// ----------------------------------------------------------------- profiles

export const HANDLE_CHANGE_COOLDOWN_DAYS = 90

export async function fetchProfileByUserId(
  userId: string,
): Promise<PlayerProfile | null> {
  const { data, errors } =
    await client.models.PlayerProfile.listPlayerProfileByUserId(
      { userId },
      { limit: 1 },
    )
  if (errors?.length) throw new Error(errors[0].message)
  return data[0] ?? null
}

async function isHandleTaken(handle: string): Promise<boolean> {
  const { data } = await client.models.PlayerProfile.listPlayerProfileByHandle(
    { handle },
    { limit: 1 },
  )
  return data.length > 0
}

/** Get the signed-in user's profile, creating one with a generated handle. */
export async function ensureMyProfile(userId: string): Promise<PlayerProfile> {
  const existing = await fetchProfileByUserId(userId)
  if (existing) return existing
  for (let attempt = 0; attempt < 6; attempt++) {
    const handle = generateHandle()
    if (await isHandleTaken(handle)) continue
    const { data, errors } = await client.models.PlayerProfile.create({
      userId,
      handle,
    })
    if (errors?.length || !data) {
      throw new Error(errors?.[0]?.message ?? 'Profile creation failed')
    }
    return data
  }
  throw new Error('Could not generate a unique handle; try again')
}

/**
 * One free rename after account creation (handleChangedAt is null until
 * then), afterwards one rename per 90 days.
 */
export function handleChangeAvailability(profile: PlayerProfile): {
  allowed: boolean
  nextChangeAt: Date | null
} {
  if (!profile.handleChangedAt) return { allowed: true, nextChangeAt: null }
  const nextChangeAt = new Date(
    new Date(profile.handleChangedAt).getTime() +
      HANDLE_CHANGE_COOLDOWN_DAYS * 24 * 60 * 60 * 1000,
  )
  return { allowed: nextChangeAt.getTime() <= Date.now(), nextChangeAt }
}

export async function changeHandle(
  profile: PlayerProfile,
  newHandle: string,
): Promise<PlayerProfile> {
  const invalid = validateHandle(newHandle)
  if (invalid) throw new Error(invalid)
  if (!handleChangeAvailability(profile).allowed) {
    throw new Error('Handle was changed recently; try again later')
  }
  if (newHandle !== profile.handle && (await isHandleTaken(newHandle))) {
    throw new Error('That handle is taken')
  }
  const { data, errors } = await client.models.PlayerProfile.update({
    id: profile.id,
    handle: newHandle,
    handleChangedAt: new Date().toISOString(),
  })
  if (errors?.length || !data) {
    throw new Error(errors?.[0]?.message ?? 'Rename failed')
  }
  return data
}

/** Most recent matches (any status) where the user was a participant. */
export async function fetchMatchHistory(
  userId: string,
  limit = 20,
): Promise<Match[]> {
  const [asDefender, asChallenger] = await Promise.all([
    client.models.Match.listMatchByDefenderUserIdAndScheduledAt(
      { defenderUserId: userId },
      { sortDirection: 'DESC', limit },
    ),
    client.models.Match.listMatchByChallengerUserIdAndScheduledAt(
      { challengerUserId: userId },
      { sortDirection: 'DESC', limit },
    ),
  ])
  const errors = asDefender.errors ?? asChallenger.errors
  if (errors?.length) throw new Error(errors[0].message)
  return [...asDefender.data, ...asChallenger.data]
    .sort((a, b) => b.scheduledAt.localeCompare(a.scheduledAt))
    .slice(0, limit)
}

/** Active matches (score confirm, dispute, etc.) where the user is a participant. */
export async function fetchMyOngoingMatches(userId: string): Promise<Match[]> {
  const recent = await fetchMatchHistory(userId, 30)
  const out: Match[] = []
  for (const m of recent) {
    const reconciled = await reconcileStaleMatch(m)
    if (isOngoingMatch(reconciled)) out.push(reconciled)
  }
  const byId = new Map(out.map((m) => [m.id, m]))
  return [...byId.values()]
}

// ------------------------------------------------------------------ matches

export async function fetchMatch(matchId: string): Promise<Match | null> {
  const { data, errors } = await client.models.Match.get({ id: matchId })
  if (errors?.length) throw new Error(errors[0].message)
  return data
}

export async function fetchMatchByChallengeId(
  challengeId: string,
): Promise<Match | null> {
  const { data, errors } = await client.models.Match.listMatchByChallengeId({
    challengeId,
  })
  if (errors?.length) throw new Error(errors[0].message)
  return data[0] ?? null
}

/** Matches at a court, most recent first. */
export async function fetchMatchesAtCourt(
  courtId: string,
  limit = 20,
): Promise<Match[]> {
  const { data, errors } =
    await client.models.Match.listMatchByCourtIdAndScheduledAt(
      { courtId },
      { sortDirection: 'DESC', limit },
    )
  if (errors?.length) throw new Error(errors[0].message)
  return data
}

/** Active/pending matches at a court; stale records are reconciled on load. */
export async function fetchOngoingMatchesAtCourt(
  courtId: string,
  limit = 20,
): Promise<Match[]> {
  const all = await fetchMatchesAtCourt(courtId, limit)
  const reconciled = await Promise.all(all.map((m) => reconcileStaleMatch(m)))
  return reconciled.filter(isOngoingMatch)
}

export const ACTIVE_MATCH_STATUSES = [
  'CONTRACTED',
  'REPORTED',
  'VERIFIED',
  'DISPUTED',
] as const

export const SCORE_POST_HOURS = 24
export const SCORE_CONFIRM_HOURS = 48

// SetScore type exported from ./scores

export function scorePostDeadline(scheduledAt: string): Date {
  return new Date(new Date(scheduledAt).getTime() + SCORE_POST_HOURS * 3_600_000)
}

export function scoreConfirmDeadline(reportedAt: string): Date {
  return new Date(new Date(reportedAt).getTime() + SCORE_CONFIRM_HOURS * 3_600_000)
}

export function canPostScore(match: Match, now = Date.now()): boolean {
  if (match.status !== 'CONTRACTED') return false
  const start = new Date(match.scheduledAt).getTime()
  return now >= start && now <= scorePostDeadline(match.scheduledAt).getTime()
}

export const TERMINAL_MATCH_STATUSES = [
  'CANCELLED',
  'VOIDED',
  'RAINED_OUT',
  'FINAL',
] as const

export const MATCH_STATUS_LABELS: Record<string, string> = {
  CONTRACTED: 'Scheduled',
  LIVE: 'In progress',
  REPORTED: 'Score posted',
  VERIFIED: 'Confirmed',
  DISPUTED: 'Disputed',
  FINAL: 'Final',
  VOIDED: 'Voided',
  RAINED_OUT: 'Rained out',
  CANCELLED: 'Cancelled',
}

/** Accepted requests stay in Scheduled only while the match is still upcoming. */
export const SCHEDULED_REQUEST_MATCH_STATUSES = ['CONTRACTED', 'LIVE'] as const

const CANCELLED_MATCH_STATUSES = new Set(['CANCELLED', 'VOIDED', 'RAINED_OUT'])

export type MatchBadgeVariant =
  | 'win'
  | 'loss'
  | 'scheduled'
  | 'live'
  | 'pending'
  | 'dispute'
  | 'action'
  | 'cancelled'

export type MatchDisplayInfo = {
  badge: string
  badgeVariant: MatchBadgeVariant
  badgeTitle: string
  actionNotice?: string
  needsAction: boolean
  showStatusInMeta: boolean
}

export type RequestMatchBucket = 'your-move' | 'waiting-on-them' | 'scheduled'

/** Bucket accepted-request matches for the Requests panel sections. */
export function requestMatchBucket(
  match: Match,
  viewerUserId: string,
  options?: { disputeStatus?: string | null; now?: number },
): RequestMatchBucket {
  const now = options?.now ?? Date.now()
  const display = matchDisplayForViewer(match, viewerUserId, {
    disputeStatus: options?.disputeStatus,
  })

  if (match.status === 'LIVE') return 'your-move'
  if (display.needsAction) return 'your-move'

  if (match.status === 'REPORTED' && match.winnerUserId === viewerUserId) {
    const confirmOpen =
      match.scoreReportedAt &&
      now <= scoreConfirmDeadline(match.scoreReportedAt).getTime()
    if (confirmOpen) return 'waiting-on-them'
  }

  if (match.status === 'DISPUTED') {
    if (options?.disputeStatus === 'AWAITING_ADMIN') return 'waiting-on-them'
    if (match.winnerUserId !== viewerUserId) return 'waiting-on-them'
  }

  if (match.status === 'VERIFIED') return 'waiting-on-them'

  return 'scheduled'
}

export function isScheduledRequestMatch(match: Match | null | undefined, now = Date.now()): boolean {
  if (!match) return false
  if (match.status === 'LIVE') return true
  if (match.status === 'CONTRACTED') {
    return now <= scorePostDeadline(match.scheduledAt).getTime()
  }
  return false
}

/** Scheduled match that has not reached its start time yet. */
export function isMatchYetToStart(match: Match | null | undefined, now = Date.now()): boolean {
  if (!match || match.status !== 'CONTRACTED') return false
  return new Date(match.scheduledAt).getTime() > now
}

/** Matches still relevant on a court panel or in active workflows. */
export function isOngoingMatch(match: Match, now = Date.now()): boolean {
  if ((TERMINAL_MATCH_STATUSES as readonly string[]).includes(match.status)) return false
  if (match.status === 'CONTRACTED') {
    return now <= scorePostDeadline(match.scheduledAt).getTime()
  }
  if (match.status === 'LIVE') return true
  if (match.status === 'REPORTED') {
    if (!match.scoreReportedAt) return true
    return now <= scoreConfirmDeadline(match.scoreReportedAt).getTime()
  }
  if (match.status === 'DISPUTED' || match.status === 'VERIFIED') return true
  return false
}

async function tryUnlockGauntlet(gauntletId: string): Promise<void> {
  try {
    const { data: gauntlet } = await client.models.Gauntlet.get({ id: gauntletId })
    if (gauntlet?.status === 'LOCKED') {
      await client.models.Gauntlet.update({ id: gauntlet.id, status: 'ACTIVE' })
    }
  } catch {
    // non-owner may not be able to unlock
  }
}

/** Auto-cancel expired scheduled matches; auto-confirm overdue score reports. */
export async function reconcileStaleMatch(match: Match, now = Date.now()): Promise<Match> {
  if (match.status === 'CONTRACTED' && now > scorePostDeadline(match.scheduledAt).getTime()) {
    const { errors } = await client.models.Match.update({ id: match.id, status: 'CANCELLED' })
    if (errors?.length) throw new Error(errors[0].message)
    await tryUnlockGauntlet(match.gauntletId)
    const updated = await fetchMatch(match.id)
    return updated ?? { ...match, status: 'CANCELLED' }
  }
  if (
    match.status === 'REPORTED' &&
    match.scoreReportedAt &&
    now > scoreConfirmDeadline(match.scoreReportedAt).getTime()
  ) {
    return autoConfirmScoreIfDue(match)
  }
  return match
}

/** Drop ACCEPTED requests whose match is no longer upcoming. */
export async function reconcileAcceptedChallenge(challenge: Challenge): Promise<Challenge> {
  if (challenge.status !== 'ACCEPTED') return challenge

  const match = await fetchMatchByChallengeId(challenge.id)
  if (!match) {
    const { data } = await client.models.Challenge.update({
      id: challenge.id,
      status: 'EXPIRED',
    })
    return data ?? challenge
  }

  const reconciledMatch = await reconcileStaleMatch(match)
  if (!isOngoingMatch(reconciledMatch)) {
    const { data } = await client.models.Challenge.update({
      id: challenge.id,
      status: 'EXPIRED',
    })
    return data ?? challenge
  }

  return challenge
}

export function matchDisplayForViewer(
  match: Match,
  viewerUserId?: string,
  options?: { disputeStatus?: string | null },
): MatchDisplayInfo {
  const statusLabel = MATCH_STATUS_LABELS[match.status] ?? match.status

  if (CANCELLED_MATCH_STATUSES.has(match.status)) {
    const badge =
      match.status === 'RAINED_OUT' ? 'R' : match.status === 'VOIDED' ? 'V' : '✕'
    return {
      badge,
      badgeVariant: 'cancelled',
      badgeTitle: statusLabel,
      needsAction: false,
      showStatusInMeta: true,
    }
  }

  if (match.status === 'FINAL' && match.winnerUserId && viewerUserId) {
    const won = match.winnerUserId === viewerUserId
    return {
      badge: won ? 'W' : 'L',
      badgeVariant: won ? 'win' : 'loss',
      badgeTitle: won ? 'Win' : 'Loss',
      needsAction: false,
      showStatusInMeta: false,
    }
  }

  if (match.status === 'REPORTED' && viewerUserId) {
    const postedWin = match.winnerUserId === viewerUserId
    const confirmOpen =
      match.scoreReportedAt &&
      Date.now() <= scoreConfirmDeadline(match.scoreReportedAt).getTime()
    if (postedWin) {
      return {
        badge: '…',
        badgeVariant: 'pending',
        badgeTitle: 'Score awaiting confirmation',
        actionNotice: confirmOpen
          ? 'Waiting for opponent to confirm score'
          : 'Score posted — confirmation window closed',
        needsAction: false,
        showStatusInMeta: true,
      }
    }
    return {
      badge: '!',
      badgeVariant: 'action',
      badgeTitle: 'Confirm the posted score',
      actionNotice: confirmOpen
        ? 'Confirm or dispute the posted score'
        : 'Score posted — open match for details',
      needsAction: Boolean(confirmOpen),
      showStatusInMeta: true,
    }
  }

  if (match.status === 'DISPUTED' && viewerUserId) {
    const postedWin = match.winnerUserId === viewerUserId
    const awaitingAdmin = options?.disputeStatus === 'AWAITING_ADMIN'
    if (awaitingAdmin) {
      return {
        badge: '⚖',
        badgeVariant: 'dispute',
        badgeTitle: 'Awaiting admin review',
        actionNotice: 'An admin will resolve this disputed score',
        needsAction: false,
        showStatusInMeta: true,
      }
    }
    if (postedWin) {
      return {
        badge: '!',
        badgeVariant: 'action',
        badgeTitle: 'Score disputed',
        actionNotice: 'Amend your score or stand by for admin review',
        needsAction: true,
        showStatusInMeta: true,
      }
    }
    return {
      badge: '…',
      badgeVariant: 'pending',
      badgeTitle: 'Score disputed',
      actionNotice: 'Waiting for winner to amend or escalate',
      needsAction: false,
      showStatusInMeta: true,
    }
  }

  if (match.status === 'VERIFIED') {
    return {
      badge: '…',
      badgeVariant: 'pending',
      badgeTitle: 'Score confirmed',
      actionNotice: 'Finalizing match',
      needsAction: false,
      showStatusInMeta: true,
    }
  }

  if (match.status === 'CONTRACTED') {
    if (viewerUserId && canPostScore(match)) {
      return {
        badge: '!',
        badgeVariant: 'action',
        badgeTitle: 'Post your score',
        actionNotice: 'Post score if you won',
        needsAction: true,
        showStatusInMeta: true,
      }
    }
    return {
      badge: '⏱',
      badgeVariant: 'scheduled',
      badgeTitle: statusLabel,
      needsAction: false,
      showStatusInMeta: !viewerUserId,
    }
  }

  if (match.status === 'LIVE') {
    return {
      badge: '●',
      badgeVariant: 'live',
      badgeTitle: statusLabel,
      needsAction: false,
      showStatusInMeta: true,
    }
  }

  return {
    badge: '·',
    badgeVariant: 'pending',
    badgeTitle: statusLabel,
    needsAction: false,
    showStatusInMeta: true,
  }
}

export async function fetchMatchMessages(matchId: string): Promise<MatchMessage[]> {
  const all: MatchMessage[] = []
  let nextToken: string | null | undefined
  do {
    const { data, errors, nextToken: token } =
      await client.models.MatchMessage.listMatchMessageByMatchIdAndSentAt(
        { matchId },
        { sortDirection: 'ASC', limit: 100, nextToken },
      )
    if (errors?.length) throw new Error(errors[0].message)
    all.push(...data)
    nextToken = token
  } while (nextToken && all.length < 500)
  return all
}

export async function sendMatchMessage(input: {
  match: Match
  senderUserId: string
  body: string
}): Promise<MatchMessage> {
  const { data, errors } = await client.models.MatchMessage.create({
    matchId: input.match.id,
    senderUserId: input.senderUserId,
    participants: input.match.participants,
    body: input.body.trim(),
    sentAt: new Date().toISOString(),
  })
  if (errors?.length || !data) {
    throw new Error(errors?.[0]?.message ?? 'Message failed')
  }
  return data
}

export async function fetchScoreReports(matchId: string): Promise<ScoreReport[]> {
  const { data, errors } = await client.models.ScoreReport.listScoreReportByMatchId({
    matchId,
  })
  if (errors?.length) throw new Error(errors[0].message)
  return data
}

export async function fetchDispute(matchId: string): Promise<Dispute | null> {
  const { data, errors } = await client.models.Dispute.listDisputeByMatchId({ matchId })
  if (errors?.length) throw new Error(errors[0].message)
  return data[0] ?? null
}

/** Winner posts the score within 24h of the scheduled time. */
export async function submitScoreReport(input: {
  match: Match
  reporterUserId: string
  setScores: SetScore[]
}): Promise<void> {
  const { match, reporterUserId, setScores } = input
  if (!canPostScore(match)) {
    throw new Error('Score can only be posted within 24 hours after the scheduled time')
  }
  const existing = await fetchScoreReports(match.id)
  if (existing.length) throw new Error('A score has already been posted')

  const now = new Date().toISOString()
  const scoresJson = toAwsJson(setScores)
  const { errors: reportErrors } = await client.models.ScoreReport.create({
    matchId: match.id,
    reporterUserId,
    participants: match.participants,
    setScores: scoresJson,
    claimedWinnerUserId: reporterUserId,
  })
  if (reportErrors?.length) throw new Error(reportErrors[0].message)

  const { errors } = await client.models.Match.update({
    id: match.id,
    status: 'REPORTED',
    scoreReportedAt: now,
    finalSetScores: scoresJson,
    winnerUserId: reporterUserId,
  })
  if (errors?.length) throw new Error(errors[0].message)
}

/** Apply Elo + W/L updates for a FINAL match (idempotent). */
export async function applyMatchRatings(matchId: string): Promise<unknown> {
  const { data, errors } = await client.mutations.applyMatchRatings({ matchId })
  if (errors?.length) throw new Error(errors[0].message)
  return data
}

/** Backfill ratings for FINAL matches that finalized before Lambda existed. */
export async function syncPendingMatchRatings(userId: string): Promise<void> {
  const history = await fetchMatchHistory(userId, 30)
  for (const m of history) {
    if (m.status !== 'FINAL' || !m.winnerUserId) continue
    if (!m.participants.includes(userId)) continue
    const { data: events } = await client.models.RatingEvent.listRatingEventByMatchId({
      matchId: m.id,
    })
    if (events.length > 0) continue
    try {
      await applyMatchRatings(m.id)
    } catch {
      // Best-effort; sandbox may need redeploy for the mutation
    }
  }
}

/** Loser confirms the posted score. */
export async function confirmPostedScore(
  match: Match,
  report: ScoreReport,
  confirmerUserId: string,
): Promise<void> {
  if (match.status !== 'REPORTED') throw new Error('No score awaiting confirmation')
  if (confirmerUserId === report.claimedWinnerUserId) {
    throw new Error('The winner cannot confirm their own score')
  }
  if (!match.participants.includes(confirmerUserId)) throw new Error('Not a participant')

  const now = new Date().toISOString()
  const scoresJson =
    typeof report.setScores === 'string' ? report.setScores : toAwsJson(report.setScores)
  const { errors: verifyErrors } = await client.models.Match.update({
    id: match.id,
    status: 'VERIFIED',
    finalSetScores: scoresJson,
    winnerUserId: report.claimedWinnerUserId,
  })
  if (verifyErrors?.length) throw new Error(verifyErrors[0].message)

  const { errors } = await client.models.Match.update({
    id: match.id,
    status: 'FINAL',
    completedAt: now,
  })
  if (errors?.length) throw new Error(errors[0].message)

  try {
    await applyMatchRatings(match.id)
  } catch {
    // Match is final; ratings can be retried via syncPendingMatchRatings
  }
}

/** Loser disputes the posted score within 48h of the report. */
export async function disputePostedScore(
  match: Match,
  report: ScoreReport,
  userId: string,
  reason: string,
): Promise<void> {
  if (match.status !== 'REPORTED') throw new Error('No score to dispute')
  if (userId === report.claimedWinnerUserId) {
    throw new Error('The winner cannot dispute their own score')
  }
  if (!match.scoreReportedAt) throw new Error('Missing report timestamp')
  if (Date.now() > scoreConfirmDeadline(match.scoreReportedAt).getTime()) {
    throw new Error('The confirmation window has closed')
  }

  const trimmed = reason.trim()
  if (!trimmed) throw new Error('Enter a reason for the dispute')

  const existing = await fetchDispute(match.id)
  if (existing?.status === 'AWAITING_ADMIN') {
    throw new Error('This dispute is already with an admin')
  }

  if (existing) {
    const { errors: disputeErrors } = await client.models.Dispute.update({
      id: existing.id,
      status: 'OPEN',
      reason: trimmed,
      resolutionNote: null,
      resolvedByUserId: null,
    })
    if (disputeErrors?.length) throw new Error(disputeErrors[0].message)
  } else {
    const { errors: disputeErrors } = await client.models.Dispute.create({
      matchId: match.id,
      raisedByUserId: userId,
      participants: match.participants,
      status: 'OPEN',
      reason: trimmed,
    })
    if (disputeErrors?.length) throw new Error(disputeErrors[0].message)
  }

  const { errors } = await client.models.Match.update({ id: match.id, status: 'DISPUTED' })
  if (errors?.length) throw new Error(errors[0].message)
}

/** Winner updates their score after a dispute; loser must confirm again. */
export async function amendDisputedScore(input: {
  match: Match
  report: ScoreReport
  dispute: Dispute
  winnerUserId: string
  setScores: SetScore[]
}): Promise<void> {
  const { match, report, dispute, winnerUserId, setScores } = input
  if (match.status !== 'DISPUTED') throw new Error('Match is not disputed')
  if (winnerUserId !== report.claimedWinnerUserId) {
    throw new Error('Only the posted winner can amend the score')
  }
  if (dispute.status !== 'OPEN') {
    throw new Error('Score can no longer be amended — awaiting admin review')
  }

  const now = new Date().toISOString()
  const scoresJson = toAwsJson(setScores)
  const { errors: reportErrors } = await client.models.ScoreReport.update({
    id: report.id,
    setScores: scoresJson,
    claimedWinnerUserId: winnerUserId,
  })
  if (reportErrors?.length) throw new Error(reportErrors[0].message)

  const { errors } = await client.models.Match.update({
    id: match.id,
    status: 'REPORTED',
    scoreReportedAt: now,
    finalSetScores: scoresJson,
    winnerUserId,
  })
  if (errors?.length) throw new Error(errors[0].message)
}

/** Winner stands by their score and escalates to admin review. */
export async function doubleDownOnDisputedScore(input: {
  match: Match
  report: ScoreReport
  dispute: Dispute
  winnerUserId: string
}): Promise<void> {
  const { match, report, dispute, winnerUserId } = input
  if (match.status !== 'DISPUTED') throw new Error('Match is not disputed')
  if (winnerUserId !== report.claimedWinnerUserId) {
    throw new Error('Only the posted winner can stand by their score')
  }
  if (dispute.status !== 'OPEN') throw new Error('Dispute has already been escalated')

  const { errors: disputeErrors } = await client.models.Dispute.update({
    id: dispute.id,
    status: 'AWAITING_ADMIN',
  })
  if (disputeErrors?.length) throw new Error(disputeErrors[0].message)

  const scoresJson =
    typeof report.setScores === 'string' ? report.setScores : toAwsJson(report.setScores)
  const { errors } = await client.models.Match.update({
    id: match.id,
    status: 'DISPUTED',
    finalSetScores: scoresJson,
    winnerUserId: report.claimedWinnerUserId,
  })
  if (errors?.length) throw new Error(errors[0].message)
}

/** If 48h passed with no response, auto-confirm the posted score. */
export async function autoConfirmScoreIfDue(match: Match): Promise<Match> {
  if (match.status !== 'REPORTED' || !match.scoreReportedAt) return match
  if (Date.now() <= scoreConfirmDeadline(match.scoreReportedAt).getTime()) return match

  const reports = await fetchScoreReports(match.id)
  const report = reports[0]
  if (!report) return match

  await confirmPostedScore(match, report, report.claimedWinnerUserId === match.defenderUserId
    ? match.challengerUserId
    : match.defenderUserId)

  const updated = await fetchMatch(match.id)
  return updated ?? match
}

/** Back out of a scheduled match before a score is posted. */
export async function cancelMatch(match: Match, userId: string): Promise<void> {
  if (!match.participants.includes(userId)) throw new Error('Not a participant')
  if (match.status !== 'CONTRACTED') {
    throw new Error('This match can no longer be cancelled')
  }

  const { errors } = await client.models.Match.update({
    id: match.id,
    status: 'CANCELLED',
  })
  if (errors?.length) throw new Error(errors[0].message)

  try {
    const { data: challenge } = await client.models.Challenge.get({ id: match.challengeId })
    if (challenge?.status === 'ACCEPTED') {
      await client.models.Challenge.update({ id: challenge.id, status: 'EXPIRED' })
    }
  } catch {
    // best-effort
  }

  try {
    await tryUnlockGauntlet(match.gauntletId)
  } catch {
    // non-owner may not be able to unlock
  }
}

// --------------------------------------------------------------------- admin
export type AdminDisputeResolution = 'uphold' | 'overturn' | 'void'

/** Remove a match and its messages, score reports, dispute, and rating events. */
export async function adminDeleteMatch(matchId: string): Promise<void> {
  const messages = await fetchMatchMessages(matchId)
  for (const msg of messages) {
    const { errors } = await client.models.MatchMessage.delete({ id: msg.id })
    if (errors?.length) throw new Error(errors[0].message)
  }

  const reports = await fetchScoreReports(matchId)
  for (const report of reports) {
    const { errors } = await client.models.ScoreReport.delete({ id: report.id })
    if (errors?.length) throw new Error(errors[0].message)
  }

  const dispute = await fetchDispute(matchId)
  if (dispute) {
    const { errors } = await client.models.Dispute.delete({ id: dispute.id })
    if (errors?.length) throw new Error(errors[0].message)
  }

  const { data: ratingEvents } = await client.models.RatingEvent.listRatingEventByMatchId({
    matchId,
  })
  for (const event of ratingEvents) {
    const { errors } = await client.models.RatingEvent.delete({ id: event.id })
    if (errors?.length) throw new Error(errors[0].message)
  }

  const { errors } = await client.models.Match.delete({ id: matchId })
  if (errors?.length) throw new Error(errors[0].message)
}

/** Admin finalizes or voids a disputed match. */
export async function adminResolveDispute(input: {
  match: Match
  dispute: Dispute
  report: ScoreReport | null
  adminUserId: string
  resolution: AdminDisputeResolution
  overturnWinnerUserId?: string
  resolutionNote?: string
}): Promise<void> {
  const { match, dispute, report, adminUserId, resolution, overturnWinnerUserId, resolutionNote } =
    input
  if (match.status !== 'DISPUTED') throw new Error('Match is not disputed')

  const now = new Date().toISOString()
  const note = resolutionNote?.trim() || null

  if (resolution === 'void') {
    const { errors: disputeErrors } = await client.models.Dispute.update({
      id: dispute.id,
      status: 'MATCH_VOIDED',
      resolutionNote: note,
      resolvedByUserId: adminUserId,
    })
    if (disputeErrors?.length) throw new Error(disputeErrors[0].message)

    const { errors } = await client.models.Match.update({
      id: match.id,
      status: 'VOIDED',
      completedAt: now,
    })
    if (errors?.length) throw new Error(errors[0].message)
    return
  }

  if (!report) throw new Error('Score report required to uphold or overturn')

  const winnerUserId =
    resolution === 'uphold' ? report.claimedWinnerUserId : overturnWinnerUserId
  if (!winnerUserId || !match.participants.includes(winnerUserId)) {
    throw new Error('Pick a valid winner')
  }

  const scoresJson =
    typeof report.setScores === 'string' ? report.setScores : toAwsJson(report.setScores)

  const { errors: disputeErrors } = await client.models.Dispute.update({
    id: dispute.id,
    status: resolution === 'uphold' ? 'RESOLVED_UPHELD' : 'RESOLVED_OVERTURNED',
    resolutionNote: note,
    resolvedByUserId: adminUserId,
  })
  if (disputeErrors?.length) throw new Error(disputeErrors[0].message)

  const { errors: verifyErrors } = await client.models.Match.update({
    id: match.id,
    status: 'VERIFIED',
    finalSetScores: scoresJson,
    winnerUserId,
  })
  if (verifyErrors?.length) throw new Error(verifyErrors[0].message)

  const { errors: finalErrors } = await client.models.Match.update({
    id: match.id,
    status: 'FINAL',
    completedAt: now,
  })
  if (finalErrors?.length) throw new Error(finalErrors[0].message)

  try {
    await applyMatchRatings(match.id)
  } catch {
    // Match is final; ratings can be retried later
  }
}

async function fetchAllMatchesAtCourt(courtId: string): Promise<Match[]> {
  const all: Match[] = []
  let nextToken: string | null | undefined
  do {
    const { data, errors, nextToken: token } =
      await client.models.Match.listMatchByCourtIdAndScheduledAt(
        { courtId },
        { limit: 100, nextToken },
      )
    if (errors?.length) throw new Error(errors[0].message)
    all.push(...data)
    nextToken = token
  } while (nextToken)
  return all
}

async function fetchAllGauntletsAtCourt(courtId: string): Promise<Gauntlet[]> {
  const all: Gauntlet[] = []
  let nextToken: string | null | undefined
  do {
    const { data, errors, nextToken: token } = await client.models.Gauntlet.list({
      filter: { courtId: { eq: courtId } },
      limit: 100,
      nextToken,
    })
    if (errors?.length) throw new Error(errors[0].message)
    all.push(...data)
    nextToken = token
  } while (nextToken)
  return all
}

async function fetchAllChallengesForGauntlet(gauntletId: string): Promise<Challenge[]> {
  const all: Challenge[] = []
  let nextToken: string | null | undefined
  do {
    const { data, errors, nextToken: token } = await client.models.Challenge.list({
      filter: { gauntletId: { eq: gauntletId } },
      limit: 100,
      nextToken,
    })
    if (errors?.length) throw new Error(errors[0].message)
    all.push(...data)
    nextToken = token
  } while (nextToken)
  return all
}

/** Remove a court and all gauntlets, challenges, and matches tied to it. */
export async function adminDeleteCourt(courtId: string): Promise<void> {
  const matches = await fetchAllMatchesAtCourt(courtId)
  for (const match of matches) {
    await adminDeleteMatch(match.id)
  }

  const gauntlets = await fetchAllGauntletsAtCourt(courtId)
  for (const gauntlet of gauntlets) {
    const challenges = await fetchAllChallengesForGauntlet(gauntlet.id)
    for (const challenge of challenges) {
      const { errors } = await client.models.Challenge.delete({ id: challenge.id })
      if (errors?.length) throw new Error(errors[0].message)
    }
    const { errors } = await client.models.Gauntlet.delete({ id: gauntlet.id })
    if (errors?.length) throw new Error(errors[0].message)
  }

  const { errors } = await client.models.Court.delete({ id: courtId })
  if (errors?.length) throw new Error(errors[0].message)
}

/** Admin override for Elo and W/L / completion counters on a profile. */
export async function adminUpdatePlayerStats(input: {
  profile: PlayerProfile
  globalElo?: number
  wins?: number
  losses?: number
  matchesCompleted?: number
}): Promise<PlayerProfile> {
  const { profile, globalElo, wins, losses, matchesCompleted } = input
  const patch: {
    id: string
    globalElo?: number
    wins?: number
    losses?: number
    matchesCompleted?: number
  } = { id: profile.id }

  if (globalElo !== undefined) {
    if (!Number.isInteger(globalElo) || globalElo < 0) throw new Error('Invalid Elo')
    patch.globalElo = globalElo
  }
  if (wins !== undefined) {
    if (!Number.isInteger(wins) || wins < 0) throw new Error('Invalid wins')
    patch.wins = wins
  }
  if (losses !== undefined) {
    if (!Number.isInteger(losses) || losses < 0) throw new Error('Invalid losses')
    patch.losses = losses
  }
  if (matchesCompleted !== undefined) {
    if (!Number.isInteger(matchesCompleted) || matchesCompleted < 0) {
      throw new Error('Invalid matches completed')
    }
    patch.matchesCompleted = matchesCompleted
  }

  if (Object.keys(patch).length === 1) throw new Error('No stats to update')

  const { data, errors } = await client.models.PlayerProfile.update(patch)
  if (errors?.length) throw new Error(errors[0].message)
  if (!data) throw new Error('Profile update failed')
  return data
}

export function formatDateTime(iso: string): string {
  const d = new Date(iso)
  const day = d.toLocaleDateString(undefined, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
  })
  const time = d.toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' })
  return `${day} · ${time}`
}

/** datetime-local input value for `now + offsetMinutes`, in local time. */
export function localInputValue(offsetMinutes: number): string {
  const d = new Date(Date.now() + offsetMinutes * 60_000)
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset(), 0, 0)
  return d.toISOString().slice(0, 16)
}
