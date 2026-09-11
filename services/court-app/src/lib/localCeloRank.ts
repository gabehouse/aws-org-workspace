import { client, type PlayerProfile } from './data'
import { eloToCelo } from './celoDisplay'

export type LocalCeloRank = {
  celo: number
  rank: number
  total: number
  /** Higher = better within the home region (e.g. top 90% beats 90% of players). */
  percentile: number
}

/** All profiles whose saved home region label matches (city/province, not map radius). */
async function fetchProfilesInHomeRegion(regionLabel: string): Promise<PlayerProfile[]> {
  const all: PlayerProfile[] = []
  let nextToken: string | null | undefined
  do {
    const { data, errors, nextToken: token } =
      await client.models.PlayerProfile.listPlayerProfileByHomeRegionLabel(
        { homeRegionLabel: regionLabel },
        { limit: 200, nextToken },
      )
    if (errors?.length) throw new Error(errors[0].message)
    all.push(...data)
    nextToken = token
  } while (nextToken && all.length < 5000)
  return all
}

/** Rank by Celo among players in the same home region label. */
export async function fetchLocalCeloRank(
  regionLabel: string,
  userId: string,
  globalElo: number,
): Promise<LocalCeloRank | null> {
  if (!regionLabel.trim()) return null

  const inRegion = await fetchProfilesInHomeRegion(regionLabel)
  if (inRegion.length === 0) {
    return {
      celo: eloToCelo(globalElo),
      rank: 1,
      total: 1,
      percentile: 100,
    }
  }

  const sorted = [...inRegion].sort(
    (a, b) => (b.globalElo ?? 1200) - (a.globalElo ?? 1200),
  )
  const rank = sorted.findIndex((p) => p.userId === userId) + 1
  const total = inRegion.length
  const effectiveRank = rank > 0 ? rank : total + 1
  const percentile =
    total <= 1 ? 100 : Math.max(1, Math.round(((total - effectiveRank + 1) / total) * 100))

  return {
    celo: eloToCelo(globalElo),
    rank: effectiveRank,
    total,
    percentile,
  }
}
