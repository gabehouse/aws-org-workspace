import { useCallback, useEffect, useMemo, useRef, useState, type PointerEvent } from 'react'
import { fetchMatch, type Court, type Match } from '../lib/data'
import {
  adviceForMoment,
  adviceFromWeather,
  guidanceForScene,
  roboCaddyMoment,
  type RoboCaddyAdvice,
  type RoboCaddyScene,
} from '../lib/roboCaddyAdvice'
import {
  getRoboCaddyFabBottom,
  maxRoboCaddyFabBottom,
  MIN_FAB_BOTTOM,
  setRoboCaddyFabBottom,
} from '../lib/roboCaddyPrefs'
import { fetchMatchHourWeather } from '../lib/weather'

interface RoboCaddyProps {
  enabled: boolean
  userId: string
  scene: RoboCaddyScene
  matchId?: string | null
  court?: Court | null
}

const DRAG_THRESHOLD_PX = 6

export function RoboCaddy({
  enabled,
  userId,
  scene,
  matchId = null,
  court = null,
}: RoboCaddyProps) {
  const [bubbleOpen, setBubbleOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const [weatherError, setWeatherError] = useState<string | null>(null)
  const [weatherAdvice, setWeatherAdvice] = useState<RoboCaddyAdvice | null>(null)
  const [match, setMatch] = useState<Match | null>(null)
  const [fabBottom, setFabBottom] = useState(() => getRoboCaddyFabBottom(userId))
  const [nowMs, setNowMs] = useState(() => Date.now())
  const [isDragging, setIsDragging] = useState(false)

  const dragRef = useRef<{
    pointerId: number
    startY: number
    startBottom: number
    dragged: boolean
  } | null>(null)
  const fabBottomRef = useRef(fabBottom)
  fabBottomRef.current = fabBottom

  const clampBottom = useCallback((value: number) => {
    return Math.max(MIN_FAB_BOTTOM, Math.min(maxRoboCaddyFabBottom(), value))
  }, [])

  const matchMoment = useMemo(
    () => (match ? roboCaddyMoment(match, userId, nowMs) : 'hidden'),
    [match, userId, nowMs],
  )

  const matchAdvice = useMemo(() => {
    if (!match || matchMoment === 'hidden') return null
    if (matchMoment === 'upcoming') {
      if (weatherError) {
        return { moment: 'upcoming' as const, message: weatherError }
      }
      if (loading) {
        return { moment: 'upcoming' as const, message: 'Sniffing the forecast…' }
      }
      return weatherAdvice
    }
    return adviceForMoment(matchMoment, match, userId, nowMs)
  }, [match, matchMoment, userId, nowMs, weatherAdvice, weatherError, loading])

  const guidanceAdvice = useMemo(() => {
    if (scene === 'match') return null
    const rotationBucket = Math.floor(nowMs / 600_000)
    return guidanceForScene(scene, `${userId}-${scene}-${rotationBucket}`)
  }, [scene, userId, nowMs])

  const display = scene === 'match' ? matchAdvice : guidanceAdvice

  useEffect(() => {
    const tick = window.setInterval(() => setNowMs(Date.now()), 30_000)
    return () => window.clearInterval(tick)
  }, [])

  useEffect(() => {
    const onResize = () => setFabBottom((b) => clampBottom(b))
    window.addEventListener('resize', onResize)
    return () => window.removeEventListener('resize', onResize)
  }, [clampBottom])

  useEffect(() => {
    if (!enabled || scene !== 'match' || !matchId) {
      setMatch(null)
      return
    }
    const controller = new AbortController()
    void fetchMatch(matchId).then((m) => {
      if (!controller.signal.aborted) setMatch(m)
    })
    return () => controller.abort()
  }, [enabled, scene, matchId])

  useEffect(() => {
    if (!enabled || scene !== 'match' || !court || !match || matchMoment !== 'upcoming') {
      setWeatherAdvice(null)
      setWeatherError(null)
      setLoading(false)
      return
    }

    const controller = new AbortController()
    setLoading(true)
    setWeatherError(null)
    setWeatherAdvice(null)

    void (async () => {
      try {
        const weather = await fetchMatchHourWeather(
          court.lat,
          court.lng,
          match.scheduledAt,
          controller.signal,
        )
        if (controller.signal.aborted) return
        if (!weather) {
          setWeatherError('Forecast not available yet — check back closer to match day.')
          return
        }
        setWeatherAdvice(adviceFromWeather(weather))
      } catch (e) {
        if (controller.signal.aborted) return
        if (e instanceof DOMException && e.name === 'AbortError') return
        setWeatherError('Could not fetch weather. Robo Caddy will try again later.')
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    })()

    return () => controller.abort()
  }, [enabled, scene, court, match, matchMoment])

  const onFabPointerDown = (e: PointerEvent<HTMLButtonElement>) => {
    e.currentTarget.setPointerCapture(e.pointerId)
    dragRef.current = {
      pointerId: e.pointerId,
      startY: e.clientY,
      startBottom: fabBottomRef.current,
      dragged: false,
    }
  }

  const onFabPointerMove = (e: PointerEvent<HTMLButtonElement>) => {
    const drag = dragRef.current
    if (!drag || drag.pointerId !== e.pointerId) return
    const deltaY = drag.startY - e.clientY
    if (Math.abs(deltaY) > DRAG_THRESHOLD_PX) {
      drag.dragged = true
      setIsDragging(true)
    }
    const next = clampBottom(drag.startBottom + deltaY)
    fabBottomRef.current = next
    setFabBottom(next)
  }

  const endFabDrag = (e: PointerEvent<HTMLButtonElement>) => {
    const drag = dragRef.current
    if (!drag || drag.pointerId !== e.pointerId) return
    e.currentTarget.releasePointerCapture(e.pointerId)
    if (drag.dragged) {
      setRoboCaddyFabBottom(userId, fabBottomRef.current)
    } else {
      setBubbleOpen((v) => !v)
    }
    setIsDragging(false)
    dragRef.current = null
  }

  if (!enabled) return null

  const fallbackGuidance = scene === 'match' && !matchAdvice ? guidanceForScene('map', userId) : null
  const visibleAdvice = display ?? fallbackGuidance

  return (
    <div className="robo-caddy" style={{ bottom: fabBottom }}>
      {bubbleOpen && visibleAdvice?.message && (
        <div className="robo-caddy__bubble" role="status" aria-live="polite">
          <p className="robo-caddy__bubble-title">Robo Caddy says</p>
          <p className="robo-caddy__bubble-text">{visibleAdvice.message}</p>
          {visibleAdvice.extras?.map((text, i) => (
            <p key={i} className="robo-caddy__bubble-extra">
              {text}
            </p>
          ))}
          {visibleAdvice.meta && <p className="robo-caddy__bubble-meta">{visibleAdvice.meta}</p>}
        </div>
      )}

      <button
        type="button"
        className={`robo-caddy__fab${bubbleOpen && visibleAdvice?.message ? ' robo-caddy__fab--open' : ''}${
          isDragging ? ' robo-caddy__fab--dragging' : ''
        }`}
        aria-label={
          bubbleOpen && visibleAdvice?.message
            ? 'Hide Robo Caddy tips'
            : 'Show Robo Caddy tips'
        }
        title="Robo Caddy — drag up/down to reposition"
        onPointerDown={onFabPointerDown}
        onPointerMove={onFabPointerMove}
        onPointerUp={endFabDrag}
        onPointerCancel={endFabDrag}
      >
        <span className="robo-caddy__dog" aria-hidden>
          <svg viewBox="0 0 64 64" className="robo-caddy__dog-svg">
            <rect x="14" y="22" width="36" height="28" rx="10" fill="#e8ece9" stroke="#2f7a3d" strokeWidth="2" />
            <circle cx="24" cy="34" r="4" fill="#2f7a3d" />
            <circle cx="40" cy="34" r="4" fill="#2f7a3d" />
            <circle cx="25" cy="33" r="1.2" fill="#fff" />
            <circle cx="41" cy="33" r="1.2" fill="#fff" />
            <rect x="28" y="40" width="8" height="4" rx="2" fill="#5a6a5e" />
            <path d="M12 26 L6 18 M52 26 L58 18" stroke="#2f7a3d" strokeWidth="3" strokeLinecap="round" />
            <rect x="26" y="14" width="12" height="10" rx="4" fill="#d4e8d7" stroke="#2f7a3d" strokeWidth="2" />
            <circle cx="32" cy="10" r="3" fill="#f4d03f" stroke="#2f7a3d" strokeWidth="1.5" />
          </svg>
        </span>
      </button>
    </div>
  )
}
