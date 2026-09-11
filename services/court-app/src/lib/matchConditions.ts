export type MatchConditionsInput = {
  /** Minutes from local midnight at the court (e.g. 19:30 → 1170) */
  localMinutes: number
  temperatureC: number
  windKmh: number
  uvIndex: number | null
  /** Max pollen grains/m³ across species when available */
  pollenGrains: number | null
}

export type ConditionWarning = {
  priority: number
  id: string
  message: string
}

export function evaluateMatchConditions(input: MatchConditionsInput): ConditionWarning[] {
  const warnings: ConditionWarning[] = []
  const { localMinutes, temperatureC, windKmh, uvIndex, pollenGrains } = input

  if (pollenGrains != null && pollenGrains >= 60) {
    warnings.push({
      priority: 100,
      id: 'pollen',
      message:
        'Pollen alert! Allergy sufferers: pre-medicate and keep a towel handy for watery eyes between points.',
    })
  }

  if (uvIndex != null && uvIndex >= 8) {
    warnings.push({
      priority: 95,
      id: 'uv-extreme',
      message:
        'UV is brutal out there — sunscreen, a hat, and reapply between sets or you will cook.',
    })
  } else if (uvIndex != null && uvIndex >= 6) {
    warnings.push({
      priority: 90,
      id: 'uv-high',
      message: 'High UV expected. Slap on SPF 30+ before you walk on court.',
    })
  }

  if (temperatureC > 30) {
    warnings.push({
      priority: 80,
      id: 'heat',
      message:
        "Extreme heat warning! The court is going to radiate like an oven. Bring electrolyte tabs, a cold towel, and drink water starting two hours before.",
    })
  }

  if (windKmh > 22) {
    warnings.push({
      priority: 75,
      id: 'wind',
      message:
        "Hang onto your hat. The wind is whipping. Forget pretty flat shots—keep your margins high, hit with heavy spin, and expect weird ball curves.",
    })
  }

  if (temperatureC < 12) {
    warnings.push({
      priority: 60,
      id: 'cold',
      message:
        "It's going to be chilly. The tennis balls will feel like heavy rocks and won't bounce. Warm up your joints and don't expect deep baselines.",
    })
  }

  if (localMinutes >= 19 * 60 + 30) {
    warnings.push({
      priority: 50,
      id: 'lights',
      message:
        'Playing under the lights! The air will get damp, slowing down the court. Watch out for a slippery baseline if dew settles on the lines.',
    })
  }

  if (warnings.length === 0) {
    warnings.push({
      priority: 0,
      id: 'nice',
      message:
        'Perfect hitting weather. Bend your knees, step into your forehand, and remember to look the ball into the center of the strings.',
    })
  }

  return warnings.sort((a, b) => b.priority - a.priority)
}

export function primaryWarning(warnings: ConditionWarning[]): ConditionWarning {
  return warnings[0]
}
