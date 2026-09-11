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

export async function geocode(query: string, signal?: AbortSignal): Promise<GeocodeResult[]> {
  const url = new URL('https://nominatim.openstreetmap.org/search')
  url.searchParams.set('q', query)
  url.searchParams.set('format', 'jsonv2')
  url.searchParams.set('limit', '5')
  const res = await fetch(url, {
    signal,
    headers: { Accept: 'application/json' },
  })
  if (!res.ok) throw new Error(`Search failed (${res.status})`)
  const places = (await res.json()) as NominatimPlace[]
  return places.map((p) => ({
    label: p.display_name,
    lat: Number(p.lat),
    lng: Number(p.lon),
    bbox: p.boundingbox?.map(Number) as GeocodeResult['bbox'],
  }))
}
