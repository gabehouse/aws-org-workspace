import { HomePinPopover } from './HomePinPopover'

function HomePinIcon() {
  return (
    <svg className="home-marker__icon-svg" viewBox="0 0 24 24" aria-hidden="true">
      <path
        d="M12 4.5 7 9v10h3.2v-4.8h3.6V19H17V9l-5-4.5Z"
        fill="currentColor"
      />
    </svg>
  )
}

interface HomeMarkerProps {
  regionLabel: string
  userId: string
  globalElo: number
  popoverOpen: boolean
  dragging?: boolean
  saving?: boolean
}

export function HomeMarker({
  regionLabel,
  userId,
  globalElo,
  popoverOpen,
  dragging = false,
  saving = false,
}: HomeMarkerProps) {
  return (
    <div
      className={`home-marker-wrap${dragging ? ' home-marker-wrap--dragging' : ''}${popoverOpen ? ' home-marker-wrap--open' : ''}`}
    >
      {popoverOpen && !dragging && (
        <HomePinPopover
          regionLabel={regionLabel}
          userId={userId}
          globalElo={globalElo}
          saving={saving}
        />
      )}
      <div
        className={[
          'home-marker',
          popoverOpen ? 'home-marker--active' : '',
          dragging ? 'home-marker--dragging' : '',
        ]
          .filter(Boolean)
          .join(' ')}
        aria-hidden="true"
      >
        <span className="home-marker__icon">
          <HomePinIcon />
        </span>
      </div>
    </div>
  )
}

export function HomeMarkerPreview({ placing = false }: { placing?: boolean }) {
  return (
    <span
      className={[
        'home-marker',
        'home-marker--preview',
        placing ? 'home-marker--active' : '',
      ]
        .filter(Boolean)
        .join(' ')}
      aria-hidden="true"
    >
      <span className="home-marker__icon">
        <HomePinIcon />
      </span>
    </span>
  )
}
