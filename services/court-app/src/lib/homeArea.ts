import type { PlayerProfile } from './data'
import type { HomeArea } from './homeRegion'

export function homeAreaFromProfile(profile: PlayerProfile): HomeArea | null {
  if (
    profile.homeRegionLabel == null ||
    profile.homeLat == null ||
    profile.homeLng == null
  ) {
    return null
  }

  return {
    regionLabel: profile.homeRegionLabel,
    lat: profile.homeLat,
    lng: profile.homeLng,
  }
}
