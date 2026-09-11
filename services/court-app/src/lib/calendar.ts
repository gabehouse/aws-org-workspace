import type { Court, Match } from './data'

const DEFAULT_DURATION_MINUTES = 120

function toGoogleCalendarUtc(iso: string): string {
  return new Date(iso).toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '')
}

export function buildGoogleCalendarUrl(params: {
  title: string
  startAt: string
  durationMinutes?: number
  location?: string
  details?: string
}): string {
  const start = new Date(params.startAt)
  const end = new Date(
    start.getTime() + (params.durationMinutes ?? DEFAULT_DURATION_MINUTES) * 60_000,
  )
  const dates = `${toGoogleCalendarUtc(start.toISOString())}/${toGoogleCalendarUtc(end.toISOString())}`
  const url = new URL('https://calendar.google.com/calendar/render')
  url.searchParams.set('action', 'TEMPLATE')
  url.searchParams.set('text', params.title)
  url.searchParams.set('dates', dates)
  if (params.details) url.searchParams.set('details', params.details)
  if (params.location) url.searchParams.set('location', params.location)
  return url.toString()
}

export function googleCalendarUrlForMatch(
  match: Match,
  court: Court | null | undefined,
  handles: Record<string, string>,
): string {
  const defender = handles[match.defenderUserId] ?? 'Host'
  const challenger = handles[match.challengerUserId] ?? 'Requester'
  const location = court
    ? [court.name, court.address].filter(Boolean).join(' — ')
    : undefined
  const details = [
    court?.name && `Court: ${court.name}`,
    match.stakes && `Stakes: ${match.stakes}`,
    match.format && `Format: ${match.format}`,
  ]
    .filter(Boolean)
    .join('\n')

  return buildGoogleCalendarUrl({
    title: `Tennis: ${defender} vs ${challenger}`,
    startAt: match.scheduledAt,
    location,
    details: details || undefined,
  })
}

export function openGoogleCalendar(url: string): void {
  window.open(url, '_blank', 'noopener,noreferrer')
}
