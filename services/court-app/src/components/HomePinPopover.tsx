import { useEffect, useState } from 'react'
import { fetchLocalCeloRank, type LocalCeloRank } from '../lib/localCeloRank'

interface HomePinPopoverProps {
  regionLabel: string
  userId: string
  globalElo: number
  saving?: boolean
}

export function HomePinPopover({
  regionLabel,
  userId,
  globalElo,
  saving = false,
}: HomePinPopoverProps) {
  const [rank, setRank] = useState<LocalCeloRank | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    void fetchLocalCeloRank(regionLabel, userId, globalElo)
      .then((result) => {
        if (!cancelled) setRank(result)
      })
      .catch(() => {
        if (!cancelled) setRank(null)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [regionLabel, userId, globalElo])

  return (
    <div className="home-pin-popover" onClick={(e) => e.stopPropagation()}>
      <p className="home-pin-popover__region">{regionLabel}</p>
      {loading && <p className="home-pin-popover__meta">Loading regional rank…</p>}
      {!loading && rank && (
        <p className="home-pin-popover__meta">
          <strong>{rank.celo.toFixed(1)} Celo</strong>
          {' · '}
          #{rank.rank} of {rank.total} in region
          {' · '}
          top {rank.percentile}%
        </p>
      )}
      {!loading && !rank && (
        <p className="home-pin-popover__meta">Regional rank unavailable</p>
      )}
      {saving && <p className="home-pin-popover__meta">Updating location…</p>}
      <p className="home-pin-popover__drag-hint">Drag the pin to move</p>
    </div>
  )
}
