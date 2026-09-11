import { useCallback, useEffect, useRef, useState } from 'react'
import type { Challenge, Dispute, Match } from './data'
import {
  fetchRequestsSnapshot,
  playRequestNotificationSound,
  requestsNotificationKeys,
} from './requestsNotifications'

const DEFAULT_POLL_MS = 12_000

export function useRequestsLiveUpdates(
  userId: string,
  options?: { pollMs?: number; enabled?: boolean },
) {
  const pollMs = options?.pollMs ?? DEFAULT_POLL_MS
  const enabled = options?.enabled ?? true

  const [challenges, setChallenges] = useState<Challenge[]>([])
  const [ongoingMatches, setOngoingMatches] = useState<Match[]>([])
  const [disputes, setDisputes] = useState<Record<string, Dispute | null>>({})
  const [loading, setLoading] = useState(true)

  const prevKeysRef = useRef<Set<string> | null>(null)
  const refreshIdRef = useRef(0)

  const refresh = useCallback(async () => {
    const id = ++refreshIdRef.current
    try {
      const snap = await fetchRequestsSnapshot(userId)
      if (id !== refreshIdRef.current) return

      setChallenges(snap.challenges)
      setOngoingMatches(snap.ongoingMatches)
      setDisputes(snap.disputes)

      const keys = requestsNotificationKeys(
        snap.challenges,
        snap.ongoingMatches,
        snap.disputes,
        userId,
      )

      if (prevKeysRef.current !== null) {
        for (const key of keys) {
          if (!prevKeysRef.current.has(key)) {
            playRequestNotificationSound()
            break
          }
        }
      }
      prevKeysRef.current = keys
    } finally {
      if (id === refreshIdRef.current) setLoading(false)
    }
  }, [userId])

  useEffect(() => {
    if (!enabled) return

    prevKeysRef.current = null
    setLoading(true)
    void refresh()

    const timer = window.setInterval(() => void refresh(), pollMs)
    const onVisible = () => {
      if (document.visibilityState === 'visible') void refresh()
    }
    document.addEventListener('visibilitychange', onVisible)

    return () => {
      window.clearInterval(timer)
      document.removeEventListener('visibilitychange', onVisible)
    }
  }, [enabled, pollMs, refresh])

  return { challenges, ongoingMatches, disputes, loading, refresh }
}
