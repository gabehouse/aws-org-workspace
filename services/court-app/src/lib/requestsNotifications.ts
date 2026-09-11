import {
  fetchDispute,
  fetchMyChallenges,
  fetchMyOngoingMatches,
  requestMatchBucket,
  type Challenge,
  type Dispute,
  type Match,
} from './data'
import {
  acknowledgeChallenges,
  isChallengeAcknowledged,
  isChallengeViewed,
  isMatchViewed,
  markChallengesViewed,
  markMatchesViewed,
} from './seenState'

export type RequestsSnapshot = {
  challenges: Challenge[]
  ongoingMatches: Match[]
  disputes: Record<string, Dispute | null>
}

export async function fetchRequestsSnapshot(userId: string): Promise<RequestsSnapshot> {
  const [challenges, ongoingMatches] = await Promise.all([
    fetchMyChallenges(userId),
    fetchMyOngoingMatches(userId),
  ])
  const disputed = ongoingMatches.filter((m) => m.status === 'DISPUTED')
  const disputes: Record<string, Dispute | null> = {}
  if (disputed.length) {
    const pairs = await Promise.all(
      disputed.map(async (m) => [m.id, await fetchDispute(m.id)] as const),
    )
    Object.assign(disputes, Object.fromEntries(pairs))
  }
  return { challenges, ongoingMatches, disputes }
}

/** Stable keys for items that should light up the Requests badge. */
export function requestsNotificationKeys(
  challenges: Challenge[],
  ongoingMatches: Match[],
  disputes: Record<string, Dispute | null>,
  userId: string,
): Set<string> {
  const keys = new Set<string>()

  for (const c of challenges) {
    if (
      (c.status === 'PENDING' || c.status === 'COUNTERED') &&
      c.proposedByUserId !== userId
    ) {
      keys.add(`challenge-move:${c.id}`)
    }
    if (
      c.status === 'ACCEPTED' &&
      c.proposedByUserId === userId &&
      !isChallengeAcknowledged(userId, c.id)
    ) {
      keys.add(`challenge-accepted:${c.id}`)
    }
  }

  for (const m of ongoingMatches) {
    if (
      requestMatchBucket(m, userId, { disputeStatus: disputes[m.id]?.status }) ===
      'your-move'
    ) {
      keys.add(`match-move:${m.id}:${m.status}`)
    }
  }

  return keys
}

/** Notification keys the user has not opened or cleared yet. */
export function unviewedRequestsNotificationKeys(
  challenges: Challenge[],
  ongoingMatches: Match[],
  disputes: Record<string, Dispute | null>,
  userId: string,
): Set<string> {
  const keys = requestsNotificationKeys(challenges, ongoingMatches, disputes, userId)
  const unviewed = new Set<string>()

  for (const key of keys) {
    if (key.startsWith('challenge-move:')) {
      const id = key.slice('challenge-move:'.length)
      if (!isChallengeViewed(userId, id)) unviewed.add(key)
    } else if (key.startsWith('challenge-accepted:')) {
      const id = key.slice('challenge-accepted:'.length)
      if (!isChallengeAcknowledged(userId, id)) unviewed.add(key)
    } else if (key.startsWith('match-move:')) {
      const matchId = key.split(':')[1]
      if (matchId && !isMatchViewed(userId, matchId)) unviewed.add(key)
    }
  }

  return unviewed
}

/** Mark every current Requests notification item as seen. */
export function markRequestsNotificationsSeen(
  challenges: Challenge[],
  ongoingMatches: Match[],
  disputes: Record<string, Dispute | null>,
  userId: string,
): void {
  const keys = requestsNotificationKeys(challenges, ongoingMatches, disputes, userId)
  const challengeIds: string[] = []
  const matchIds: string[] = []
  const acceptedIds: string[] = []

  for (const key of keys) {
    if (key.startsWith('challenge-move:')) {
      challengeIds.push(key.slice('challenge-move:'.length))
    } else if (key.startsWith('challenge-accepted:')) {
      acceptedIds.push(key.slice('challenge-accepted:'.length))
    } else if (key.startsWith('match-move:')) {
      const matchId = key.split(':')[1]
      if (matchId) matchIds.push(matchId)
    }
  }

  markChallengesViewed(userId, challengeIds)
  markMatchesViewed(userId, matchIds)
  acknowledgeChallenges(userId, acceptedIds)
}

let audioContext: AudioContext | null = null

function getAudioContext(): AudioContext | null {
  try {
    if (!audioContext) audioContext = new AudioContext()
    if (audioContext.state === 'suspended') void audioContext.resume()
    return audioContext
  } catch {
    return null
  }
}

/** Short two-tone ping for a new request or match needing attention. */
export function playRequestNotificationSound(): void {
  const ctx = getAudioContext()
  if (!ctx) return

  const playTone = (frequency: number, start: number, duration: number) => {
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.type = 'sine'
    osc.frequency.value = frequency
    gain.gain.setValueAtTime(0.0001, start)
    gain.gain.exponentialRampToValueAtTime(0.12, start + 0.02)
    gain.gain.exponentialRampToValueAtTime(0.0001, start + duration)
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start(start)
    osc.stop(start + duration)
  }

  const t = ctx.currentTime
  playTone(880, t, 0.12)
  playTone(1174.66, t + 0.14, 0.16)
}

/** Call after a user gesture so later notification sounds are allowed. */
export function primeRequestNotificationAudio(): void {
  getAudioContext()
}
