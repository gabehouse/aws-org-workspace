function loadSet(key: string): Set<string> {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return new Set()
    return new Set(JSON.parse(raw) as string[])
  } catch {
    return new Set()
  }
}

function saveSet(key: string, set: Set<string>): void {
  localStorage.setItem(key, JSON.stringify([...set]))
}

const ackKey = (userId: string) => `court-app-ack-challenges-${userId}`
const viewedMatchesKey = (userId: string) => `court-app-viewed-matches-${userId}`
const viewedChallengesKey = (userId: string) => `court-app-viewed-challenges-${userId}`

export function getAcknowledgedChallenges(userId: string): Set<string> {
  return loadSet(ackKey(userId))
}

export function acknowledgeChallenges(userId: string, challengeIds: string[]): void {
  if (challengeIds.length === 0) return
  const set = getAcknowledgedChallenges(userId)
  for (const id of challengeIds) set.add(id)
  saveSet(ackKey(userId), set)
}

export function isChallengeAcknowledged(userId: string, challengeId: string): boolean {
  return getAcknowledgedChallenges(userId).has(challengeId)
}

export function getViewedMatches(userId: string): Set<string> {
  return loadSet(viewedMatchesKey(userId))
}

export function markMatchViewed(userId: string, matchId: string): void {
  markMatchesViewed(userId, [matchId])
}

export function markMatchesViewed(userId: string, matchIds: string[]): void {
  if (matchIds.length === 0) return
  const set = getViewedMatches(userId)
  for (const id of matchIds) set.add(id)
  saveSet(viewedMatchesKey(userId), set)
}

export function isMatchViewed(userId: string, matchId: string): boolean {
  return getViewedMatches(userId).has(matchId)
}

export function getViewedChallenges(userId: string): Set<string> {
  return loadSet(viewedChallengesKey(userId))
}

export function markChallengesViewed(userId: string, challengeIds: string[]): void {
  if (challengeIds.length === 0) return
  const set = getViewedChallenges(userId)
  for (const id of challengeIds) set.add(id)
  saveSet(viewedChallengesKey(userId), set)
}

export function isChallengeViewed(userId: string, challengeId: string): boolean {
  return getViewedChallenges(userId).has(challengeId)
}

/** Accepted requests where the user was waiting on the opponent. */
export function countUnacknowledgedAccepted(
  challenges: { id: string; status: string; proposedByUserId: string }[],
  userId: string,
): number {
  const acked = getAcknowledgedChallenges(userId)
  return challenges.filter(
    (c) =>
      c.status === 'ACCEPTED' &&
      c.proposedByUserId === userId &&
      !acked.has(c.id),
  ).length
}
