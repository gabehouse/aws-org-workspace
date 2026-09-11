import { useCallback, useEffect, useState, type RefObject } from 'react'
import type { CSSProperties } from 'react'
import type { Map as MapLibreMap } from 'maplibre-gl'
import type { MapRef } from 'react-map-gl/maplibre'

export type AnchoredPanelStyle = CSSProperties & {
  top: number
  left: number
  maxHeight: number
}

export type MapPanelLayout = {
  court?: AnchoredPanelStyle
  match?: AnchoredPanelStyle
  pin?: { x: number; bottom: number }
}

const GAP = 4
/** Space between pin and court panel (half of the previous ~44px gap). */
const COURT_PANEL_GAP = 20
const COURT_W = 400
const MATCH_W = 420

/** Read the live map header height so pin placement clears the toolbar. */
export function measureHeaderHeight(): number {
  const el = document.querySelector('.map-header')
  return el ? Math.ceil(el.getBoundingClientRect().height) : 52
}

/** Pin bottom edge target — below the toolbar with a comfortable margin. */
export function targetPinBottomY(): number {
  const vw = window.innerWidth
  const margin = vw < 720 ? 44 : 52
  return measureHeaderHeight() + margin
}

/** Pan map so the court pin sits near the top, leaving room for panels below. */
export function focusPinForPanels(
  map: MapLibreMap,
  court: { lat: number; lng: number },
): void {
  const h = map.getContainer().clientHeight
  const targetY = targetPinBottomY()
  // easeTo places center at viewport midpoint + offset; pin anchor is bottom at that point
  const offsetY = Math.round(targetY - h / 2)

  map.easeTo({
    center: [court.lng, court.lat],
    offset: [0, offsetY],
    duration: 500,
  })

  map.once('moveend', () => {
    // Re-measure after pan in case header layout changed
    const refinedY = targetPinBottomY()
    const refinedOffset = Math.round(refinedY - h / 2)
    if (Math.abs(refinedOffset - offsetY) > 4) {
      map.easeTo({
        center: [court.lng, court.lat],
        offset: [0, refinedOffset],
        duration: 200,
      })
    }
  })
}

function panelWidth(vw: number, desktop: number): number {
  return vw < 720 ? vw - 32 : desktop
}

function panelLeft(pinX: number, panelW: number, vw: number): number {
  if (vw < 720) return 16
  return Math.round(Math.max(16, Math.min(pinX - panelW / 2, vw - panelW - 16)))
}

function toPanelStyle(top: number, left: number, maxHeight: number, width: number, vw: number): AnchoredPanelStyle {
  const mobileWidth = 'calc(100vw - 32px)'
  return {
    position: 'absolute',
    top,
    left,
    bottom: 'auto',
    right: 'auto',
    width: vw < 720 ? mobileWidth : `min(${width}px, calc(100vw - 32px))`,
    maxWidth: vw < 720 ? mobileWidth : undefined,
    maxHeight,
    margin: 0,
    boxSizing: 'border-box',
  }
}

function computeLayout(
  pinX: number,
  pinBottom: number,
  vw: number,
  vh: number,
  courtOpen: boolean,
  matchOpen: boolean,
): MapPanelLayout {
  const courtTop = Math.round(Math.max(pinBottom + GAP, targetPinBottomY() + COURT_PANEL_GAP))
  const maxBelow = Math.max(120, vh - courtTop - 12)
  const courtW = panelWidth(vw, COURT_W)
  const matchW = panelWidth(vw, MATCH_W)
  const isMobile = vw < 720

  if (courtOpen && matchOpen) {
    if (!isMobile && vw >= 900) {
      const courtLeft = Math.max(16, Math.round(pinX - courtW - GAP))
      const matchLeft = Math.min(vw - matchW - 16, Math.round(pinX + GAP))
      return {
        court: toPanelStyle(courtTop, courtLeft, maxBelow, courtW, vw),
        match: toPanelStyle(courtTop, matchLeft, maxBelow, matchW, vw),
      }
    }
    // Match takes full vertical space; court panel stays hidden (pin only).
    return {
      match: toPanelStyle(courtTop, panelLeft(pinX, matchW, vw), maxBelow, matchW, vw),
    }
  }

  if (courtOpen) {
    return {
      court: toPanelStyle(courtTop, panelLeft(pinX, courtW, vw), maxBelow, courtW, vw),
    }
  }

  if (matchOpen) {
    return {
      match: toPanelStyle(courtTop, panelLeft(pinX, matchW, vw), maxBelow, matchW, vw),
    }
  }

  return {}
}

export function useMapPanelAnchor(
  mapRef: RefObject<MapRef | null>,
  court: { lat: number; lng: number } | null,
  enabled: boolean,
  options: { courtOpen: boolean; matchOpen: boolean },
): { layout: MapPanelLayout | null; recenterPin: () => void } {
  const [layout, setLayout] = useState<MapPanelLayout | null>(null)

  const recenterPin = useCallback(() => {
    if (!court || !mapRef.current) return
    focusPinForPanels(mapRef.current.getMap(), court)
  }, [court, mapRef])

  useEffect(() => {
    if (!enabled || !court || !mapRef.current) {
      setLayout(null)
      return
    }

    const map = mapRef.current.getMap()

    const update = () => {
      const pt = map.project([court.lng, court.lat])
      const pinBottom = pt.y
      const pinX = pt.x
      setLayout({
        pin: { x: pinX, bottom: pinBottom },
        ...computeLayout(
          pinX,
          pinBottom,
          window.innerWidth,
          window.innerHeight,
          options.courtOpen,
          options.matchOpen,
        ),
      })
    }

    const onMove = () => update()
    update()
    map.on('move', onMove)
    map.on('moveend', onMove)
    map.on('resize', onMove)
    window.addEventListener('resize', onMove)
    return () => {
      map.off('move', onMove)
      map.off('moveend', onMove)
      map.off('resize', onMove)
      window.removeEventListener('resize', onMove)
    }
  }, [enabled, court, mapRef, options.courtOpen, options.matchOpen])

  return { layout, recenterPin }
}
