import type { Match } from './data'
import {
  canPostScore,
  isMatchYetToStart,
  scorePostDeadline,
} from './data'
import { evaluateMatchConditions, primaryWarning } from './matchConditions'
import type { MatchHourWeather } from './weather'

const CANCELLED = new Set(['CANCELLED', 'VOIDED', 'RAINED_OUT'])

export type RoboCaddyMoment = 'upcoming' | 'in-play' | 'win' | 'loss' | 'hidden'

export type RoboCaddyScene = 'map' | 'court' | 'challenges' | 'match'

export type RoboCaddyAdvice = {
  moment: RoboCaddyMoment | RoboCaddyScene
  message: string
  extras?: string[]
  meta?: string
}

/** Past start time, score not yet posted (CONTRACTED/LIVE in the play window). */
export function isMatchInPlayWindow(match: Match, now = Date.now()): boolean {
  if (match.status === 'LIVE') return true
  if (match.status === 'CONTRACTED') {
    const start = new Date(match.scheduledAt).getTime()
    return now >= start && now <= scorePostDeadline(match.scheduledAt).getTime()
  }
  return false
}

export function roboCaddyMoment(
  match: Match | null | undefined,
  viewerUserId: string,
  now = Date.now(),
): RoboCaddyMoment {
  if (!match || !match.participants.includes(viewerUserId)) return 'hidden'
  if (CANCELLED.has(match.status)) return 'hidden'

  if (match.status === 'FINAL' && match.winnerUserId) {
    return match.winnerUserId === viewerUserId ? 'win' : 'loss'
  }

  if (isMatchYetToStart(match, now)) return 'upcoming'
  if (isMatchInPlayWindow(match, now)) return 'in-play'

  return 'hidden'
}

function pickQuote(quotes: string[], seed: string): string {
  let h = 0
  for (let i = 0; i < seed.length; i++) h = (h + seed.charCodeAt(i) * 31) | 0
  return quotes[Math.abs(h) % quotes.length]!
}

const IN_PLAY_QUOTES = [
  "You're on court — unclench your jaw, breathe out on contact, and play this point only.",
  'Mid-match check: sip water, reset between points, and trust the swing you already own.',
  'Long rally or short — exhale before you serve, soften your grip, and let your feet do the work.',
  'If the score is tight, slow your walk between points. Calm body, clear target, one ball at a time.',
  'Robo nose says: the next point matters more than the last one. Breathe in through the nose, play loose.',
]

const WIN_QUOTES = [
  'What a battle — you earned that one. Shake hands, hydrate, and let yourself enjoy the win.',
  'Victory logged! Great fight out there. Cool down, stretch, and carry that confidence into the next hit.',
  'You got the W — strong work. Savor it for a minute, then note one thing you did well for next time.',
  'Champion energy today. Congratulate your opponent too — good tennis needs both sides showing up.',
]

const LOSS_QUOTES = [
  "Tough one — losses sting, but they teach faster than wins. You showed up; that already counts.",
  'Robo hug: every pro has days like this. Rest, refuel, and come back hungry — not hard on yourself.',
  "You fought well even if the scoreboard disagrees. One match doesn't define you — your next session does.",
  'Loss today, lesson banked. Stretch tonight, sleep well, and the court will still be there tomorrow.',
]

const LATE_POST_QUOTES = [
  "Clock's ticking on posting — when you're ready, log the score so your opponent isn't left hanging.",
]

export function adviceForMoment(
  moment: RoboCaddyMoment,
  match: Match,
  viewerUserId: string,
  now = Date.now(),
): RoboCaddyAdvice {
  const seed = `${match.id}-${viewerUserId}-${moment}`

  if (moment === 'in-play') {
    const quotes = [...IN_PLAY_QUOTES]
    if (canPostScore(match, now)) {
      quotes.push(pickQuote(LATE_POST_QUOTES, seed + '-post'))
    }
    return {
      moment,
      message: pickQuote(quotes, seed),
      meta: 'On court now',
    }
  }

  if (moment === 'win') {
    return {
      moment,
      message: pickQuote(WIN_QUOTES, seed),
      meta: 'Match final — win',
    }
  }

  if (moment === 'loss') {
    return {
      moment,
      message: pickQuote(LOSS_QUOTES, seed),
      meta: 'Match final — loss',
    }
  }

  return { moment: 'hidden', message: '' }
}

export function adviceFromWeather(weather: MatchHourWeather): RoboCaddyAdvice {
  const warnings = evaluateMatchConditions({
    localMinutes: weather.localMinutes,
    temperatureC: weather.temperatureC,
    windKmh: weather.windKmh,
    uvIndex: weather.uvIndex,
    pollenGrains: weather.pollenGrains,
  })
  const lead = primaryWarning(warnings)
  const extras = warnings
    .filter((w) => w.id !== lead.id && w.priority >= 50)
    .map((w) => w.message)

  return {
    moment: 'upcoming',
    message: lead.message,
    extras: extras.length ? extras : undefined,
    meta: `Forecast for ${new Date(weather.forecastHour).toLocaleString(undefined, {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    })}`,
  }
}

const MAP_GUIDANCE = [
  'Tap a court pin on the map to find open matches, or drop your gauntlet and start accepting challenges.',
  'New here? Pick a court pin to browse who wants to play, or open Requests to answer incoming challenges.',
]

const COURT_GUIDANCE = [
  'Drop a gauntlet and you are telling the universe you are free. The universe might send a challenger — be ready.',
  'Post when you can actually play. Wishful scheduling is how friendly matches become calendar archaeology.',
  'Accepting challenges is voluntary — but so is cardio. Both make you a better tennis human.',
  'Robo wisdom: the best rival is someone who shows up on time with fresh balls and no excuses.',
  "A gauntlet isn't a commitment to play every stranger — it's an open door. You're allowed to close it.",
  'Philosophy hour: every match starts as a polite negotiation between two people who could be napping instead.',
  'Tip from the cart path: lead with your real availability. Optimism is great; no-shows are not.',
  'Someone might challenge you here. Accept boldly, decline kindly, nudge realistically — all are respectable shots.',
  'Courts are public stages for small acts of courage. Posting a gauntlet counts as one.',
  'Humor helps: treat your gauntlet note like a handshake, not a terms-of-service document.',
  'The net is low; your standards for scheduling can be high. Propose times you will actually honor.',
  'Robo Caddy believes every good rivalry begins with someone brave enough to say "I am free Thursday."',
]

const CHALLENGES_GUIDANCE = [
  "Someone's waiting on your reply — a quick accept, nudge, or decline beats leaving them on read.",
  'Robo Caddy begs: ghosting opponents is worse than shanking an easy volley. Respond when you can.',
  "A request on your side means their evening is in limbo. Accept, nudge, or decline — fortune favors the communicators.",
  "Your move on the clock? Opponents can't plan their day until you answer. Don't be that court.",
  'Tennis karma is real: reply promptly now, and others reply promptly when it is your challenge.',
  'Leaving a request unanswered is like holding serve forever — eventually people walk off court.',
  "Quick check-in: if they're waiting on you, a clear yes, no, or new time respects everyone's schedule.",
  'The ball is in your court literally and figuratively. Lob back a decision when you are ready.',
  'Opponents are not NPCs. A fast reply is the difference between sportsmanship and suspense horror.',
  'Even a polite decline is a gift — it frees them to find another hit. Silence is not a strategy.',
  'Robo nose detects limbo. If you can play, accept. If not, nudge or decline like a decent mammal.',
  'Waiting is the unforced error of matchmaking. Clear the request queue like you clear the net.',
]

export function guidanceForScene(
  scene: Exclude<RoboCaddyScene, 'match'>,
  seed = '',
): RoboCaddyAdvice {
  const pools = {
    map: MAP_GUIDANCE,
    court: COURT_GUIDANCE,
    challenges: CHALLENGES_GUIDANCE,
  } as const
  const quotes = pools[scene]
  const message = pickQuote(quotes, `${scene}-${seed}`)

  const meta =
    scene === 'map'
      ? 'Getting started'
      : scene === 'court'
        ? 'Court panel'
        : 'Requests'

  return { moment: scene, message, meta }
}
