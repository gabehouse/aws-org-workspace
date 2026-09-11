const key = (userId: string) => `court-app-robo-caddy-${userId}`

/** Robo Caddy is on by default; players can disable it in account settings. */
export function isRoboCaddyEnabled(userId: string): boolean {
  try {
    const raw = localStorage.getItem(key(userId))
    if (raw === null) return true
    return raw === 'true'
  } catch {
    return true
  }
}

export function setRoboCaddyEnabled(userId: string, enabled: boolean): void {
  localStorage.setItem(key(userId), enabled ? 'true' : 'false')
}

const fabBottomKey = (userId: string) => `court-app-robo-caddy-fab-bottom-${userId}`

const DEFAULT_FAB_BOTTOM = 88
const MIN_FAB_BOTTOM = 16

export function getRoboCaddyFabBottom(userId: string): number {
  try {
    const raw = localStorage.getItem(fabBottomKey(userId))
    if (raw === null) return DEFAULT_FAB_BOTTOM
    const n = Number(raw)
    return Number.isFinite(n) ? n : DEFAULT_FAB_BOTTOM
  } catch {
    return DEFAULT_FAB_BOTTOM
  }
}

export function setRoboCaddyFabBottom(userId: string, bottom: number): void {
  localStorage.setItem(fabBottomKey(userId), String(Math.round(bottom)))
}

export function maxRoboCaddyFabBottom(): number {
  if (typeof window === 'undefined') return 400
  return Math.max(MIN_FAB_BOTTOM, window.innerHeight - 72)
}

export { MIN_FAB_BOTTOM, DEFAULT_FAB_BOTTOM }
