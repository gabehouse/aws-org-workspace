const STORAGE_KEY = 'court-app-dark-mode'
const THEME_CHANGE_EVENT = 'court-app-theme-change'

export type ThemeMode = 'light' | 'dark'

export function isDarkMode(): boolean {
  try {
    return localStorage.getItem(STORAGE_KEY) === 'true'
  } catch {
    return false
  }
}

export function getThemeMode(): ThemeMode {
  return document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light'
}

export function subscribeThemeMode(onStoreChange: () => void): () => void {
  window.addEventListener(THEME_CHANGE_EVENT, onStoreChange)
  return () => window.removeEventListener(THEME_CHANGE_EVENT, onStoreChange)
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
  window.dispatchEvent(new Event(THEME_CHANGE_EVENT))
}
