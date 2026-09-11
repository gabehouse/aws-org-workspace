export type MatchHourWeather = {
  temperatureC: number
  windKmh: number
  uvIndex: number | null
  pollenGrains: number | null
  /** Local hour label from the forecast (e.g. "2024-07-15T14:00") */
  forecastHour: string
  localMinutes: number
}

type HourlyForecast = {
  hourly?: {
    time?: string[]
    temperature_2m?: number[]
    wind_speed_10m?: number[]
    uv_index?: (number | null)[]
  }
}

type AirQualityHourly = {
  hourly?: {
    time?: string[]
    alder_pollen?: (number | null)[]
    birch_pollen?: (number | null)[]
    grass_pollen?: (number | null)[]
    mugwort_pollen?: (number | null)[]
    olive_pollen?: (number | null)[]
    ragweed_pollen?: (number | null)[]
  }
}

function parseLocalMinutes(timeLocal: string): number {
  const timePart = timeLocal.split('T')[1] ?? '00:00'
  const [h, m] = timePart.split(':').map(Number)
  return h * 60 + (m || 0)
}

function nearestHourIndex(times: string[], targetMs: number): number {
  let best = 0
  let bestDiff = Infinity
  for (let i = 0; i < times.length; i++) {
    const t = new Date(times[i]).getTime()
    const diff = Math.abs(t - targetMs)
    if (diff < bestDiff) {
      bestDiff = diff
      best = i
    }
  }
  return best
}

function maxPollen(hourly: AirQualityHourly['hourly'], idx: number): number | null {
  if (!hourly) return null
  const fields = [
    hourly.alder_pollen,
    hourly.birch_pollen,
    hourly.grass_pollen,
    hourly.mugwort_pollen,
    hourly.olive_pollen,
    hourly.ragweed_pollen,
  ]
  let max: number | null = null
  for (const series of fields) {
    const v = series?.[idx]
    if (v != null && Number.isFinite(v)) {
      max = max == null ? v : Math.max(max, v)
    }
  }
  return max
}

/** Hourly weather at the court for the scheduled match time (Open-Meteo, client-side). */
export async function fetchMatchHourWeather(
  lat: number,
  lng: number,
  scheduledAt: string,
  signal?: AbortSignal,
): Promise<MatchHourWeather | null> {
  const targetMs = new Date(scheduledAt).getTime()
  if (!Number.isFinite(targetMs)) return null

  const forecastUrl = new URL('https://api.open-meteo.com/v1/forecast')
  forecastUrl.searchParams.set('latitude', String(lat))
  forecastUrl.searchParams.set('longitude', String(lng))
  forecastUrl.searchParams.set('hourly', 'temperature_2m,wind_speed_10m,uv_index')
  forecastUrl.searchParams.set('wind_speed_unit', 'kmh')
  forecastUrl.searchParams.set('timezone', 'auto')
  forecastUrl.searchParams.set('forecast_days', '16')

  const aqUrl = new URL('https://air-quality-api.open-meteo.com/v1/air-quality')
  aqUrl.searchParams.set('latitude', String(lat))
  aqUrl.searchParams.set('longitude', String(lng))
  aqUrl.searchParams.set(
    'hourly',
    'alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen',
  )
  aqUrl.searchParams.set('timezone', 'auto')
  aqUrl.searchParams.set('forecast_days', '16')

  const [forecastRes, aqRes] = await Promise.all([
    fetch(forecastUrl, { signal }),
    fetch(aqUrl, { signal }).catch(() => null),
  ])

  if (!forecastRes.ok) return null

  const forecast = (await forecastRes.json()) as HourlyForecast
  const times = forecast.hourly?.time
  if (!times?.length) return null

  const idx = nearestHourIndex(times, targetMs)
  const temp = forecast.hourly?.temperature_2m?.[idx]
  const wind = forecast.hourly?.wind_speed_10m?.[idx]
  if (temp == null || wind == null) return null

  let pollen: number | null = null
  if (aqRes?.ok) {
    const aq = (await aqRes.json()) as AirQualityHourly
    const aqTimes = aq.hourly?.time
    if (aqTimes?.length) {
      const aqIdx = nearestHourIndex(aqTimes, targetMs)
      pollen = maxPollen(aq.hourly, aqIdx)
    }
  }

  const forecastHour = times[idx]
  const uv = forecast.hourly?.uv_index?.[idx] ?? null

  return {
    temperatureC: temp,
    windKmh: wind,
    uvIndex: uv != null && Number.isFinite(uv) ? uv : null,
    pollenGrains: pollen,
    forecastHour,
    localMinutes: parseLocalMinutes(forecastHour),
  }
}
