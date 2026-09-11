import type { Handler } from 'aws-lambda'
import { Amplify } from 'aws-amplify'
import { generateClient } from 'aws-amplify/data'
import { getAmplifyDataClientConfig } from '@aws-amplify/backend/function/runtime'
import type { Schema } from '../../data/resource'
import { env } from '$amplify/env/applyMatchRatings'

const DEFAULT_ELO = 1200
const K_FACTOR = 32

function computeEloUpdate(winnerRating: number, loserRating: number) {
  const expectedWinner = 1 / (1 + 10 ** ((loserRating - winnerRating) / 400))
  const expectedLoser = 1 - expectedWinner
  const winnerDelta = Math.round(K_FACTOR * (1 - expectedWinner))
  const loserDelta = Math.round(K_FACTOR * (0 - expectedLoser))
  return {
    winnerNew: winnerRating + winnerDelta,
    loserNew: loserRating + loserDelta,
    winnerDelta,
    loserDelta,
  }
}

const { resourceConfig, libraryOptions } = await getAmplifyDataClientConfig(env)
Amplify.configure(resourceConfig, libraryOptions)
const client = generateClient<Schema>()

type Args = { matchId: string }

export const handler: Handler<{ arguments: Args }> = async (event) => {
  const { matchId } = event.arguments

  const { data: match, errors: matchErrors } = await client.models.Match.get({ id: matchId })
  if (matchErrors?.length) throw new Error(matchErrors[0].message)
  if (!match) throw new Error('Match not found')
  if (match.status !== 'FINAL' || !match.winnerUserId) {
    throw new Error('Match is not final')
  }

  const { data: existingEvents, errors: eventErrors } =
    await client.models.RatingEvent.listRatingEventByMatchId({ matchId })
  if (eventErrors?.length) throw new Error(eventErrors[0].message)
  if (existingEvents.length > 0) {
    return { applied: false, reason: 'already_applied' }
  }

  const winnerId = match.winnerUserId
  const loserId = match.participants.find((id) => id !== winnerId)
  if (!loserId) throw new Error('Could not determine loser')

  const [winnerProfiles, loserProfiles] = await Promise.all([
    client.models.PlayerProfile.listPlayerProfileByUserId({ userId: winnerId }, { limit: 1 }),
    client.models.PlayerProfile.listPlayerProfileByUserId({ userId: loserId }, { limit: 1 }),
  ])

  const winnerProfile = winnerProfiles.data[0]
  const loserProfile = loserProfiles.data[0]
  if (!winnerProfile || !loserProfile) {
    throw new Error('Player profile missing')
  }

  const { winnerNew, loserNew, winnerDelta, loserDelta } = computeEloUpdate(
    winnerProfile.globalElo ?? DEFAULT_ELO,
    loserProfile.globalElo ?? DEFAULT_ELO,
  )

  const now = new Date().toISOString()

  const { errors: winnerUpdateErrors } = await client.models.PlayerProfile.update({
    id: winnerProfile.id,
    globalElo: winnerNew,
    wins: (winnerProfile.wins ?? 0) + 1,
    matchesCompleted: (winnerProfile.matchesCompleted ?? 0) + 1,
  })
  if (winnerUpdateErrors?.length) throw new Error(winnerUpdateErrors[0].message)

  const { errors: loserUpdateErrors } = await client.models.PlayerProfile.update({
    id: loserProfile.id,
    globalElo: loserNew,
    losses: (loserProfile.losses ?? 0) + 1,
    matchesCompleted: (loserProfile.matchesCompleted ?? 0) + 1,
  })
  if (loserUpdateErrors?.length) throw new Error(loserUpdateErrors[0].message)

  const { errors: winnerEventErrors } = await client.models.RatingEvent.create({
    userId: winnerId,
    matchId,
    ratingType: 'GLOBAL_ELO',
    delta: winnerDelta,
    ratingAfter: winnerNew,
    occurredAt: now,
  })
  if (winnerEventErrors?.length) throw new Error(winnerEventErrors[0].message)

  const { errors: loserEventErrors } = await client.models.RatingEvent.create({
    userId: loserId,
    matchId,
    ratingType: 'GLOBAL_ELO',
    delta: loserDelta,
    ratingAfter: loserNew,
    occurredAt: now,
  })
  if (loserEventErrors?.length) throw new Error(loserEventErrors[0].message)

  return {
    applied: true,
    winnerDelta,
    loserDelta,
    winnerNew,
    loserNew,
  }
}
