const STORAGE_KEY = 'court-app-dark-mode'

export function isDarkMode(): boolean {
  try {
    return localStorage.getItem(STORAGE_KEY) === 'true'
  } catch {
    return false
  }
}

export function setDarkMode(enabled: boolean): void {
  try {
    localStorage.setItem(STORAGE_KEY, enabled ? 'true' : 'false')
  } catch {
    // ignore quota / private mode
  }
  applyTheme(enabled)
}

/** Apply before first paint to avoid a light-mode flash. */
export function initTheme(): void {
  applyTheme(isDarkMode())
}

function applyTheme(dark: boolean): void {
  document.documentElement.dataset.theme = dark ? 'dark' : 'light'
}
