export type HomeArea = {
  regionLabel: string
  lat: number
  lng: number
}

/** Map zoom scope around the home pin (~5 km radius). */
export const HOME_VIEW_RADIUS_KM = 5

interface NominatimAddress {
  city?: string
  town?: string
  village?: string
  municipality?: string
  county?: string
  state?: string
  state_district?: string
}

interface NominatimReverse {
  display_name: string
  address?: NominatimAddress
}

function regionFromReverse(result: NominatimReverse): string {
  const address = result.address
  if (address) {
    const locality =
      address.city ??
      address.town ??
      address.village ??
      address.municipality ??
      address.county
    const province = address.state ?? address.state_district
    if (locality && province) return `${locality}, ${province}`
    if (locality) return locality
    if (province) return province
  }
  const parts = result.display_name
    .split(',')
    .map((part) => part.trim())
    .filter((part) => part !== 'Canada')
  return parts.slice(0, 2).join(', ') || result.display_name
}

async function reverseGeocode(lat: number, lng: number): Promise<NominatimReverse> {
  const url = new URL('https://nominatim.openstreetmap.org/reverse')
  url.searchParams.set('lat', String(lat))
  url.searchParams.set('lon', String(lng))
  url.searchParams.set('format', 'jsonv2')

  const res = await fetch(url, { headers: { Accept: 'application/json' } })
  if (!res.ok) throw new Error(`Location lookup failed (${res.status})`)

  const data = (await res.json()) as NominatimReverse
  if (!data.display_name) throw new Error('Could not determine region for that spot')
  return data
}

/** Bounding box for a circle of radiusKm around a point — [south, north, west, east]. */
export function bboxForRadiusKm(
  lat: number,
  lng: number,
  radiusKm: number,
): [number, number, number, number] {
  const latDelta = radiusKm / 111.32
  const lngDelta = radiusKm / (111.32 * Math.cos((lat * Math.PI) / 180))
  return [lat - latDelta, lat + latDelta, lng - lngDelta, lng + lngDelta]
}

export function homeAreaMapBounds(
  area: HomeArea,
  radiusKm = HOME_VIEW_RADIUS_KM,
): [number, number, number, number] {
  return bboxForRadiusKm(area.lat, area.lng, radiusKm)
}

/** Resolve region label from a map pin the user placed. */
export async function resolveHomeAreaFromPin(lat: number, lng: number): Promise<HomeArea> {
  const reverse = await reverseGeocode(lat, lng)
  return { regionLabel: regionFromReverse(reverse), lat, lng }
}
