export const DEFAULT_ELO = 1200
export const DEFAULT_K_FACTOR = 32

export type EloUpdate = {
  winnerNew: number
  loserNew: number
  winnerDelta: number
  loserDelta: number
}

/** Standard Elo expected-score update (winner scored 1, loser 0). */
export function computeEloUpdate(
  winnerRating: number,
  loserRating: number,
  k = DEFAULT_K_FACTOR,
): EloUpdate {
  const expectedWinner = 1 / (1 + 10 ** ((loserRating - winnerRating) / 400))
  const expectedLoser = 1 - expectedWinner
  const winnerDelta = Math.round(k * (1 - expectedWinner))
  const loserDelta = Math.round(k * (0 - expectedLoser))
  return {
    winnerNew: winnerRating + winnerDelta,
    loserNew: loserRating + loserDelta,
    winnerDelta,
    loserDelta,
  }
}
