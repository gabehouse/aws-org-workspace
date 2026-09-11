/** Best-of-3 with standard deuce games and a 7-point tiebreak at 6-6. */
export const DEFAULT_FORMAT = 'Best of 3 sets, 7-point tiebreak at 6-6.'

export type SetScore = {
  defender: number
  challenger: number
  tiebreak?: { defender: number; challenger: number }
}

export type SetInput = {
  mine: string
  theirs: string
  /** Loser's tiebreak points (winner inferred as first to 7 by 2). */
  tbLoser: string
}

export const EMPTY_SET: SetInput = { mine: '', theirs: '', tbLoser: '' }

export const DEFAULT_SET_INPUTS: SetInput[] = [
  { mine: '6', theirs: '4', tbLoser: '' },
  { mine: '6', theirs: '3', tbLoser: '' },
  EMPTY_SET,
]

/** Infer winner's tiebreak points from the loser's score (first to 7, win by 2). */
export function inferTiebreakWinner(loserPoints: number): number {
  return loserPoints < 6 ? 7 : loserPoints + 2
}

/** AppSync AWSJSON fields must be sent as JSON strings. */
export function toAwsJson(value: unknown): string {
  return JSON.stringify(value)
}

export function parseSetScores(raw: unknown): SetScore[] {
  if (!raw) return []
  if (typeof raw === 'string') {
    try {
      return parseSetScores(JSON.parse(raw))
    } catch {
      return []
    }
  }
  if (!Array.isArray(raw)) return []
  return raw.filter(
    (s): s is SetScore =>
      typeof s === 'object' &&
      s !== null &&
      'defender' in s &&
      'challenger' in s &&
      typeof (s as SetScore).defender === 'number' &&
      typeof (s as SetScore).challenger === 'number',
  )
}

export function needsTiebreakInput(mine: string, theirs: string): boolean {
  const m = Number(mine)
  const t = Number(theirs)
  if (Number.isNaN(m) || Number.isNaN(t)) return false
  return (m === 7 && t === 6) || (m === 6 && t === 7)
}

/** Best-of-3: exactly 2 reporter wins; match ends once someone has 2; no skipped sets. */
export function validateBestOfThree(sets: SetInput[]): string | null {
  let reporterWins = 0
  let opponentWins = 0
  let matchDecided = false
  let setsEntered = 0

  for (let i = 0; i < sets.length; i++) {
    const { mine, theirs } = sets[i]
    const filled = Boolean(mine.trim() && theirs.trim())
    const laterFilled = sets.slice(i + 1).some((s) => s.mine.trim() && s.theirs.trim())
    if (!filled && laterFilled) {
      return `Set ${i + 1}: can't skip sets — enter scores in order`
    }
    if (!filled) continue

    setsEntered++
    if (matchDecided) {
      return `Set ${i + 1}: match was already over — leave blank`
    }

    const myGames = Number(mine)
    const theirGames = Number(theirs)
    if (Number.isNaN(myGames) || Number.isNaN(theirGames)) {
      return `Set ${i + 1}: invalid game scores`
    }
    if (myGames === theirGames) return `Set ${i + 1}: set must have a winner`

    if (myGames > theirGames) reporterWins++
    else opponentWins++

    if (reporterWins === 2 || opponentWins === 2) matchDecided = true
  }

  if (setsEntered < 2) return 'Enter at least two sets (best of 3)'
  if (reporterWins !== 2) {
    if (reporterWins === 1 && opponentWins === 1) {
      return 'Enter set 3 — match was tied 1-1'
    }
    return 'You must win exactly 2 sets to post as winner'
  }
  return null
}

/** Pre-fill score form from an existing report (for amending). */
export function setScoresToSetInputs(
  scores: SetScore[],
  reporterIsDefender: boolean,
): SetInput[] {
  const inputs: SetInput[] = [EMPTY_SET, EMPTY_SET, EMPTY_SET]
  scores.forEach((s, i) => {
    if (i >= 3) return
    const mine = reporterIsDefender ? s.defender : s.challenger
    const theirs = reporterIsDefender ? s.challenger : s.defender
    let tbLoser = ''
    if (s.tiebreak) {
      const myTb = reporterIsDefender ? s.tiebreak.defender : s.tiebreak.challenger
      const theirTb = reporterIsDefender ? s.tiebreak.challenger : s.tiebreak.defender
      tbLoser = String(Math.min(myTb, theirTb))
    }
    inputs[i] = { mine: String(mine), theirs: String(theirs), tbLoser }
  })
  return inputs
}

/** Convert reporter-perspective inputs to defender/challenger set scores. */
export function buildSetScores(
  sets: SetInput[],
  reporterIsDefender: boolean,
): { scores: SetScore[]; error: string | null } {
  const progressionError = validateBestOfThree(sets)
  if (progressionError) return { scores: [], error: progressionError }

  const built: SetScore[] = []

  for (let i = 0; i < sets.length; i++) {
    const { mine, theirs, tbLoser } = sets[i]
    if (!mine.trim() && !theirs.trim()) continue
    if (!mine.trim() || !theirs.trim()) {
      return { scores: [], error: `Set ${i + 1}: enter both game scores` }
    }

    const myGames = Number(mine)
    const theirGames = Number(theirs)
    if (Number.isNaN(myGames) || Number.isNaN(theirGames)) {
      return { scores: [], error: `Set ${i + 1}: invalid game scores` }
    }

    const defender = reporterIsDefender ? myGames : theirGames
    const challenger = reporterIsDefender ? theirGames : myGames

    let tiebreak: SetScore['tiebreak']
    if (needsTiebreakInput(mine, theirs)) {
      if (!tbLoser.trim()) {
        return { scores: [], error: `Set ${i + 1}: enter the loser's tiebreak points` }
      }
      const loserTb = Number(tbLoser)
      if (Number.isNaN(loserTb) || loserTb < 0) {
        return { scores: [], error: `Set ${i + 1}: invalid tiebreak score` }
      }
      const winnerTb = inferTiebreakWinner(loserTb)
      const defenderWonSet = defender > challenger
      tiebreak = defenderWonSet
        ? { defender: winnerTb, challenger: loserTb }
        : { defender: loserTb, challenger: winnerTb }
    }

    const err = validateSet({ defender, challenger, tiebreak })
    if (err) return { scores: [], error: `Set ${i + 1}: ${err}` }

    built.push({ defender, challenger, ...(tiebreak ? { tiebreak } : {}) })
  }

  return { scores: built, error: null }
}

function validateSet(set: SetScore): string | null {
  const { defender, challenger, tiebreak } = set
  const hi = Math.max(defender, challenger)
  const lo = Math.min(defender, challenger)
  const defenderWon = defender > challenger

  if (hi < 6) return 'winner needs at least 6 games'
  if (hi === 6 && lo <= 4) {
    if (tiebreak) return 'tiebreak only applies to 7-6 sets'
    return null
  }
  if (hi === 7 && lo === 5) {
    if (tiebreak) return '7-5 sets do not use a tiebreak'
    return null
  }
  if (hi === 7 && lo === 6) {
    if (!tiebreak) return '7-6 set requires tiebreak score'
    const tbHi = Math.max(tiebreak.defender, tiebreak.challenger)
    const tbLo = Math.min(tiebreak.defender, tiebreak.challenger)
    if (tbHi < 7) return 'tiebreak winner needs at least 7 points'
    if (tbHi - tbLo < 2) return 'tiebreak must be won by 2'
    const tbDefenderWon = tiebreak.defender > tiebreak.challenger
    if (tbDefenderWon !== defenderWon) return 'tiebreak winner must match set winner'
    return null
  }
  return 'invalid set score — ATP: 6-0 to 6-4, 7-5, or 7-6 with tiebreak'
}

/** Count sets won from reporter's perspective. */
export function setsWonByReporter(
  scores: SetScore[],
  reporterIsDefender: boolean,
): number {
  return scores.filter((s) => {
    const reporterGames = reporterIsDefender ? s.defender : s.challenger
    const opponentGames = reporterIsDefender ? s.challenger : s.defender
    return reporterGames > opponentGames
  }).length
}

export function formatSetLine(set: SetScore, viewerIsDefender: boolean): string {
  const mine = viewerIsDefender ? set.defender : set.challenger
  const theirs = viewerIsDefender ? set.challenger : set.defender
  let line = `${mine}-${theirs}`
  if (set.tiebreak) {
    const myTb = viewerIsDefender ? set.tiebreak.defender : set.tiebreak.challenger
    const theirTb = viewerIsDefender ? set.tiebreak.challenger : set.tiebreak.defender
    const loserTb = myTb < theirTb ? myTb : theirTb
    line += `(${loserTb})`
  }
  return line
}

export function formatAllSets(
  raw: unknown,
  match: { defenderUserId: string },
  viewerUserId: string,
): string {
  const scores = parseSetScores(raw)
  const viewerIsDefender = match.defenderUserId === viewerUserId
  return scores.map((s) => formatSetLine(s, viewerIsDefender)).join(', ')
}
