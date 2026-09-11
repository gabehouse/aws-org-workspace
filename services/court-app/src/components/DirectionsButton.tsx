import type { MouseEvent } from 'react'
import { googleMapsDirectionsUrl, openDirections } from '../lib/maps'

interface DirectionsButtonProps {
  lat: number
  lng: number
  compact?: boolean
  className?: string
  onClick?: (e: MouseEvent<HTMLButtonElement>) => void
}

export function DirectionsButton({
  lat,
  lng,
  compact,
  className = '',
  onClick,
}: DirectionsButtonProps) {
  const url = googleMapsDirectionsUrl(lat, lng)

  return (
    <button
      type="button"
      className={`btn btn--calendar${compact ? ' btn--calendar-compact' : ''}${className ? ` ${className}` : ''}`}
      title="Get directions"
      aria-label="Get directions"
      onClick={(e) => {
        onClick?.(e)
        e.stopPropagation()
        openDirections(url)
      }}
    >
      {compact ? (
        <span className="btn--calendar__icon" aria-hidden>
          🧭
        </span>
      ) : (
        'Directions'
      )}
    </button>
  )
}
