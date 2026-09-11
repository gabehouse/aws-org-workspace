import { DEFAULT_ELO } from './elo'

/** Display scale calibrated so new players start near 5.0 Celo. */
export const CELO_MIN = 1
export const CELO_MAX = 16.5
export const CELO_ANCHOR = 5
export const CELO_ANCHOR_ELO = DEFAULT_ELO
/** Each 100 Elo ≈ 1 Celo point on the display scale. */
export const ELO_PER_CELO = 100

function clampCelo(celo: number): number {
  return Math.min(CELO_MAX, Math.max(CELO_MIN, celo))
}

/** Convert stored Elo to Celo for display (one decimal). */
export function eloToCelo(elo: number): number {
  const raw = CELO_ANCHOR + (elo - CELO_ANCHOR_ELO) / ELO_PER_CELO
  return Math.round(clampCelo(raw) * 10) / 10
}

/** Convert a Celo display value back to stored Elo (integer). */
export function celoToElo(celo: number): number {
  const clamped = clampCelo(celo)
  return Math.round(CELO_ANCHOR_ELO + (clamped - CELO_ANCHOR) * ELO_PER_CELO)
}

export function formatCelo(elo: number): string {
  return eloToCelo(elo).toFixed(1)
}

export function formatCeloLabel(elo: number): string {
  return `${formatCelo(elo)} Celo`
}
