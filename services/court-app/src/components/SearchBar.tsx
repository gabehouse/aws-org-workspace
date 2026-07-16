import { useEffect, useRef, useState } from 'react'
import { SearchIcon } from './MapIcons'

export interface GeocodeResult {
  label: string
  lat: number
  lng: number
  /** [south, north, west, east] when the place has an area */
  bbox?: [number, number, number, number]
}

interface NominatimPlace {
  display_name: string
  lat: string
  lon: string
  boundingbox?: [string, string, string, string]
}

async function geocode(query: string, signal: AbortSignal): Promise<GeocodeResult[]> {
  const url = new URL('https://nominatim.openstreetmap.org/search')
  url.searchParams.set('q', query)
  url.searchParams.set('format', 'jsonv2')
  url.searchParams.set('limit', '5')
  const res = await fetch(url, { signal, headers: { Accept: 'application/json' } })
  if (!res.ok) throw new Error(`Search failed (${res.status})`)
  const places = (await res.json()) as NominatimPlace[]
  return places.map((p) => ({
    label: p.display_name,
    lat: Number(p.lat),
    lng: Number(p.lon),
    bbox: p.boundingbox?.map(Number) as GeocodeResult['bbox'],
  }))
}

interface SearchBarProps {
  onSelect: (result: GeocodeResult) => void
  /** Icon button that expands inline in the map toolbar */
  compact?: boolean
}

export function SearchBar({ onSelect, compact = false }: SearchBarProps) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<GeocodeResult[]>([])
  const [open, setOpen] = useState(false)
  const [searching, setSearching] = useState(false)
  const [expanded, setExpanded] = useState(!compact)
  const abortRef = useRef<AbortController | null>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const collapse = () => {
    setExpanded(false)
    setOpen(false)
    setQuery('')
    setResults([])
    setSearching(false)
    abortRef.current?.abort()
  }

  const handleChange = (value: string) => {
    setQuery(value)
    const isSearchable = value.trim().length >= 3
    setSearching(isSearchable)
    if (!isSearchable) setResults([])
  }

  useEffect(() => {
    abortRef.current?.abort()
    const trimmed = query.trim()
    if (trimmed.length < 3) return
    const controller = new AbortController()
    abortRef.current = controller
    const timer = setTimeout(async () => {
      try {
        const found = await geocode(trimmed, controller.signal)
        setResults(found)
        setOpen(true)
      } catch (e) {
        if (!(e instanceof DOMException && e.name === 'AbortError')) setResults([])
      } finally {
        if (!controller.signal.aborted) setSearching(false)
      }
    }, 400)
    return () => clearTimeout(timer)
  }, [query])

  useEffect(() => {
    const onPointerDown = (e: PointerEvent) => {
      if (!containerRef.current?.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('pointerdown', onPointerDown)
    return () => document.removeEventListener('pointerdown', onPointerDown)
  }, [])

  useEffect(() => {
    if (expanded && compact) inputRef.current?.focus()
  }, [expanded, compact])

  if (compact && !expanded) {
    return (
      <button
        type="button"
        className="map-util-btn"
        aria-label="Search for a place"
        onClick={() => setExpanded(true)}
      >
        <SearchIcon />
      </button>
    )
  }

  return (
    <div
      className={`search-bar${compact ? ' search-bar--compact' : ''}${expanded ? ' search-bar--expanded' : ''}`}
      ref={containerRef}
    >
      <input
        ref={inputRef}
        type="search"
        value={query}
        onChange={(e) => handleChange(e.target.value)}
        onFocus={() => results.length && setOpen(true)}
        placeholder={compact ? 'City or postal code…' : 'Search city or postal code…'}
        aria-label="Search for a place"
      />
      {compact && (
        <button
          type="button"
          className="search-bar__close"
          aria-label="Close search"
          onClick={collapse}
        >
          ×
        </button>
      )}
      {searching && <span className="search-bar__spinner" aria-hidden="true" />}
      {open && results.length > 0 && (
        <ul className="search-bar__results">
          {results.map((r) => (
            <li key={`${r.lat},${r.lng}`}>
              <button
                type="button"
                onClick={() => {
                  onSelect(r)
                  setOpen(false)
                  setQuery('')
                  if (compact) collapse()
                }}
              >
                {r.label}
              </button>
            </li>
          ))}
        </ul>
      )}
      {open && !searching && results.length === 0 && query.trim().length >= 3 && (
        <ul className="search-bar__results">
          <li className="search-bar__empty">No places found</li>
        </ul>
      )}
    </div>
  )
}
