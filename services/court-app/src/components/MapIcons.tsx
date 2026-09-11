/** Small map toolbar icons — inline SVG for crisp rendering. */

export function SearchIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="11" cy="11" r="6.5" stroke="currentColor" strokeWidth="2" />
      <path d="M16 16l5 5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

export function RequestsIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M6 6h12a2 2 0 012 2v9a2 2 0 01-2 2H9l-4 3v-3H6a2 2 0 01-2-2V8a2 2 0 012-2z"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinejoin="round"
      />
    </svg>
  )
}

/** Tennis court outline with a + badge. */
export function AddCourtIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <rect
        x="4"
        y="6"
        width="16"
        height="12"
        rx="1.5"
        stroke="currentColor"
        strokeWidth="1.5"
      />
      <line x1="12" y1="6" x2="12" y2="18" stroke="currentColor" strokeWidth="1.5" />
      <line x1="4" y1="12" x2="20" y2="12" stroke="currentColor" strokeWidth="1.5" />
      <circle cx="17.5" cy="8.5" r="4.5" fill="var(--court-green)" />
      <path
        d="M17.5 6.5v4M15.5 8.5h4"
        stroke="#fff"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  )
}

export function ProfileIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <circle cx="12" cy="8" r="3.5" stroke="currentColor" strokeWidth="1.75" />
      <path
        d="M5 19c0-3.5 3.1-5.5 7-5.5s7 2 7 5.5"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinecap="round"
      />
    </svg>
  )
}
