import { HomeMarkerPreview } from './HomeMarker'

interface HomePinSettingProps {
  regionLabel?: string | null
  placing?: boolean
  busy?: boolean
  onStartPlacingHomePin?: () => void
}

export function HomePinSetting({
  regionLabel = null,
  placing = false,
  busy = false,
  onStartPlacingHomePin,
}: HomePinSettingProps) {
  const hint = placing
    ? 'Tap the map to drop your pin'
    : regionLabel ?? 'Set your general location with the home pin.'

  return (
    <div className="home-pin-setting">
      <button
        type="button"
        className={`home-pin-setting__btn${placing ? ' home-pin-setting__btn--placing' : ''}`}
        aria-label={
          regionLabel
            ? `Home: ${regionLabel}. Tap to move your home pin.`
            : 'Set your general location with the home pin'
        }
        disabled={busy}
        onClick={() => onStartPlacingHomePin?.()}
      >
        <HomeMarkerPreview placing={placing} />
      </button>
      <p className="panel__meta home-pin-setting__hint">{hint}</p>
    </div>
  )
}
