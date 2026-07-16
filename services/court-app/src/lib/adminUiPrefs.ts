const key = (userId: string) => `court-app-admin-ui-${userId}`

/** Admin tools are on by default; admins can hide them to preview the normal player UI. */
export function isAdminUiEnabled(userId: string): boolean {
  try {
    const raw = localStorage.getItem(key(userId))
    if (raw === null) return true
    return raw === 'true'
  } catch {
    return true
  }
}

export function setAdminUiEnabled(userId: string, enabled: boolean): void {
  try {
    localStorage.setItem(key(userId), enabled ? 'true' : 'false')
  } catch {
    // ignore quota / private mode
  }
}
