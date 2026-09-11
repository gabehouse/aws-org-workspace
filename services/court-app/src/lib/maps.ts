/** Google Maps directions deep link (opens app on mobile, web on desktop). */
export function googleMapsDirectionsUrl(lat: number, lng: number): string {
  const url = new URL('https://www.google.com/maps/dir/')
  url.searchParams.set('api', '1')
  url.searchParams.set('destination', `${lat},${lng}`)
  return url.toString()
}

export function openDirections(url: string): void {
  window.open(url, '_blank', 'noopener,noreferrer')
}
