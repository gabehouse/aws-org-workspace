import type { StyleSpecification } from 'maplibre-gl'
import type { ThemeMode } from './themePrefs'

/** Available basemap presets — set via VITE_MAP_BASEMAP in .env.local */
export type MapBasemapId =
  | 'carto-voyager'
  | 'carto-positron'
  | 'carto-dark'
  | 'stadia-watercolor'
  | 'stadia-alidade-smooth'
  | 'stadia-toner-lite'
  | 'mapbox-light'
  | 'mapbox-outdoors'
  | 'jawg-light'
  | 'jawg-sunny'

export type MapStyleSelection = {
  style: StyleSpecification | string
  basemapId: MapBasemapId
}

function rasterStyle(tiles: string[], attribution: string): StyleSpecification {
  return {
    version: 8,
    sources: {
      basemap: {
        type: 'raster',
        tiles,
        tileSize: 256,
        attribution,
      },
    },
    layers: [{ id: 'basemap', type: 'raster', source: 'basemap' }],
  }
}

const CARTO_ATTRIBUTION = '&copy; OpenStreetMap contributors &copy; CARTO'
const STADIA_ATTRIBUTION =
  '&copy; Stadia Maps &copy; Stamen Design &copy; OpenStreetMap contributors'
const JAWG_ATTRIBUTION = '&copy; Jawg Maps &copy; OpenStreetMap contributors'
const MAPBOX_ATTRIBUTION = '&copy; Mapbox &copy; OpenStreetMap'

function mapboxAccessToken(): string | null {
  const token = import.meta.env.VITE_MAPBOX_ACCESS_TOKEN?.trim()
  return token || null
}

/** MapLibre-safe Mapbox styles via raster tiles (vector style URLs need extra glue). */
function mapboxRasterStyle(styleId: string, basemapId: MapBasemapId): MapStyleSelection | null {
  const token = mapboxAccessToken()
  if (!token) return null
  const tiles = [
    `https://api.mapbox.com/styles/v1/mapbox/${styleId}/tiles/512/{z}/{x}/{y}?access_token=${encodeURIComponent(token)}`,
  ]
  return {
    basemapId,
    style: {
      version: 8,
      sources: {
        basemap: {
          type: 'raster',
          tiles,
          tileSize: 512,
          attribution: MAPBOX_ATTRIBUTION,
        },
      },
      layers: [{ id: 'basemap', type: 'raster', source: 'basemap' }],
    },
  }
}

const cartoTiles = (path: string) => [
  `https://a.basemaps.cartocdn.com/${path}/{z}/{x}/{y}.png`,
  `https://b.basemaps.cartocdn.com/${path}/{z}/{x}/{y}.png`,
  `https://c.basemaps.cartocdn.com/${path}/{z}/{x}/{y}.png`,
  `https://d.basemaps.cartocdn.com/${path}/{z}/{x}/{y}.png`,
]

function stadiaTiles(path: string, ext: 'png' | 'jpg' = 'png'): string[] | null {
  const key = import.meta.env.VITE_STADIA_API_KEY?.trim()
  if (!key) return null
  return [
    `https://tiles.stadiamaps.com/tiles/${path}/{z}/{x}/{y}.${ext}?api_key=${encodeURIComponent(key)}`,
  ]
}

function jawgTiles(style: string): string[] | null {
  const token = import.meta.env.VITE_JAWG_ACCESS_TOKEN?.trim()
  if (!token) return null
  return [
    `https://tile.jawg.io/${style}/{z}/{x}/{y}.png?access-token=${encodeURIComponent(token)}`,
  ]
}

const BASEMAP_BUILDERS: Record<MapBasemapId, () => MapStyleSelection | null> = {
  /** Soft pastels, lively — default for light and dark. */
  'carto-voyager': () => ({
    basemapId: 'carto-voyager',
    style: rasterStyle(cartoTiles('rastertiles/voyager'), CARTO_ATTRIBUTION),
  }),
  'carto-positron': () => ({
    basemapId: 'carto-positron',
    style: rasterStyle(cartoTiles('light_all'), CARTO_ATTRIBUTION),
  }),
  'carto-dark': () => ({
    basemapId: 'carto-dark',
    style: rasterStyle(cartoTiles('dark_all'), CARTO_ATTRIBUTION),
  }),
  'stadia-watercolor': () => {
    const tiles = stadiaTiles('stamen_watercolor', 'jpg')
    if (!tiles) return null
    return {
      basemapId: 'stadia-watercolor',
      style: rasterStyle(tiles, STADIA_ATTRIBUTION),
    }
  },
  'stadia-alidade-smooth': () => {
    const tiles = stadiaTiles('alidade_smooth')
    if (!tiles) return null
    return {
      basemapId: 'stadia-alidade-smooth',
      style: rasterStyle(tiles, STADIA_ATTRIBUTION),
    }
  },
  'stadia-toner-lite': () => {
    const tiles = stadiaTiles('stamen_toner_lite')
    if (!tiles) return null
    return {
      basemapId: 'stadia-toner-lite',
      style: rasterStyle(tiles, STADIA_ATTRIBUTION),
    }
  },
  'mapbox-light': () => mapboxRasterStyle('light-v11', 'mapbox-light'),
  'mapbox-outdoors': () => mapboxRasterStyle('outdoors-v12', 'mapbox-outdoors'),
  'jawg-light': () => {
    const tiles = jawgTiles('jawg-light')
    if (!tiles) return null
    return {
      basemapId: 'jawg-light',
      style: rasterStyle(tiles, JAWG_ATTRIBUTION),
    }
  },
  'jawg-sunny': () => {
    const tiles = jawgTiles('jawg-sunny')
    if (!tiles) return null
    return {
      basemapId: 'jawg-sunny',
      style: rasterStyle(tiles, JAWG_ATTRIBUTION),
    }
  },
}

const VALID_BASEMAPS = new Set<string>(Object.keys(BASEMAP_BUILDERS))

const DEFAULT_BASEMAP: MapBasemapId = 'carto-voyager'

function defaultBasemapId(): MapBasemapId {
  const pref = parseBasemapPref()
  if (pref) return pref
  if (mapboxAccessToken()) return 'mapbox-outdoors'
  return DEFAULT_BASEMAP
}

function parseBasemapPref(): MapBasemapId | null {
  const raw = import.meta.env.VITE_MAP_BASEMAP?.trim()
  if (!raw || !VALID_BASEMAPS.has(raw)) return null
  return raw as MapBasemapId
}

/** Same basemap in light and dark unless VITE_MAP_BASEMAP overrides. */
export function resolveMapStyle(_theme: ThemeMode): MapStyleSelection {
  const requested = defaultBasemapId()
  const built = BASEMAP_BUILDERS[requested]()
  if (built) return built

  if (import.meta.env.DEV && requested !== DEFAULT_BASEMAP) {
    console.warn(
      `[map] Basemap "${requested}" needs an API key — falling back to ${DEFAULT_BASEMAP}.`,
    )
  }
  const fallback = BASEMAP_BUILDERS[DEFAULT_BASEMAP]()
  if (!fallback) throw new Error(`Missing basemap: ${DEFAULT_BASEMAP}`)
  return fallback
}

export function mapStyleForTheme(theme: ThemeMode): StyleSpecification | string {
  return resolveMapStyle(theme).style
}

export const MAP_BASEMAP_OPTIONS: { id: MapBasemapId; label: string; key: string }[] = [
  { id: 'mapbox-outdoors', label: 'Mapbox Outdoors', key: 'VITE_MAPBOX_ACCESS_TOKEN' },
  { id: 'mapbox-light', label: 'Mapbox Light', key: 'VITE_MAPBOX_ACCESS_TOKEN' },
  { id: 'carto-voyager', label: 'CARTO Voyager (free fallback)', key: '' },
  { id: 'jawg-light', label: 'Jawg Light', key: 'VITE_JAWG_ACCESS_TOKEN' },
  { id: 'jawg-sunny', label: 'Jawg Sunny', key: 'VITE_JAWG_ACCESS_TOKEN' },
]
