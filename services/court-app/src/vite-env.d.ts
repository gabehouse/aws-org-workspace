/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Stadia Maps API key — unlocks Watercolor, Toner, Alidade styles */
  readonly VITE_STADIA_API_KEY?: string
  /** Mapbox access token — unlocks Mapbox GL styles */
  readonly VITE_MAPBOX_ACCESS_TOKEN?: string
  /** Jawg access token — unlocks Jawg raster styles */
  readonly VITE_JAWG_ACCESS_TOKEN?: string
  /**
   * Basemap preset. Defaults: light → stadia-watercolor (if Stadia key set) else carto-positron;
   * dark → stadia-toner-lite (if Stadia key) else carto-dark.
   */
  readonly VITE_MAP_BASEMAP?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
