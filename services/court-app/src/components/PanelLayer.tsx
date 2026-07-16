import type { CSSProperties, ReactNode } from 'react'

interface PanelLayerProps {
  zIndex: number
  children: ReactNode
  /** Full-screen dimmed backdrop; click passes through to onBackdropClick */
  modal?: boolean
  /** Light scrim so elevated panels (e.g. match) read above side panels */
  elevated?: boolean
  onBackdropClick?: () => void
}

/** Positions a panel in the map overlay stack with an explicit z-index. */
export function PanelLayer({
  zIndex,
  children,
  modal,
  elevated,
  onBackdropClick,
}: PanelLayerProps) {
  const style = { zIndex } satisfies CSSProperties
  if (modal) {
    return (
      <div className="panel-layer panel-layer--modal" style={style}>
        <button
          type="button"
          className="overlay-backdrop"
          onClick={onBackdropClick}
          aria-label="Close"
        />
        {children}
      </div>
    )
  }
  if (elevated) {
    return (
      <div className="panel-layer panel-layer--elevated" style={style}>
        {onBackdropClick && (
          <button
            type="button"
            className="overlay-backdrop overlay-backdrop--light"
            onClick={onBackdropClick}
            aria-label="Close"
          />
        )}
        {children}
      </div>
    )
  }
  return (
    <div className="panel-layer" style={style}>
      {children}
    </div>
  )
}
