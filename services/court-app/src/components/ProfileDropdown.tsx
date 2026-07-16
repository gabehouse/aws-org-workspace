import { useEffect, useLayoutEffect, useRef, useState, type CSSProperties, type RefObject } from 'react'
import { createPortal } from 'react-dom'
import {
  changeHandle,
  fetchDispute,
  fetchHandles,
  fetchMatchHistory,
  handleChangeAvailability,
  type Dispute,
  type Match,
  type PlayerProfile,
} from '../lib/data'
import { isDarkMode, setDarkMode } from '../lib/themePrefs'
import { ProfileCard, ProfileMatchHistory } from './ProfileCard'
import { ProfileIcon } from './MapIcons'

interface ProfileDropdownProps {
  /** null while the profile is bootstrapping on first sign-in */
  profile: PlayerProfile | null
  onProfileUpdated: (profile: PlayerProfile) => void
  onViewProfile: (userId: string) => void
  onOpenMatch: (matchId: string) => void
  signOut: () => void
  /** Compact icon button for the map toolbar */
  compact?: boolean
  menuZIndex?: number
  /** Portal target so account menu shares z-index stacking with map panels */
  portalRoot?: RefObject<HTMLElement | null>
  onOpenChange?: (open: boolean) => void
  roboCaddyEnabled?: boolean
  onRoboCaddyPrefChange?: (enabled: boolean) => void
  isAdmin?: boolean
  adminUiEnabled?: boolean
  onAdminUiPrefChange?: (enabled: boolean) => void
}

export function ProfileDropdown({
  profile,
  onProfileUpdated,
  onViewProfile,
  onOpenMatch,
  signOut,
  compact = false,
  menuZIndex = 200,
  portalRoot,
  onOpenChange,
  roboCaddyEnabled = true,
  onRoboCaddyPrefChange,
  isAdmin = false,
  adminUiEnabled = true,
  onAdminUiPrefChange,
}: ProfileDropdownProps) {
  const [open, setOpen] = useState(false)
  const [menuStyle, setMenuStyle] = useState<CSSProperties>({})
  const [menuFillHeight, setMenuFillHeight] = useState(false)
  const [matches, setMatches] = useState<Match[] | null>(null)
  const [disputes, setDisputes] = useState<Record<string, Dispute | null>>({})
  const [opponentHandles, setOpponentHandles] = useState<Record<string, string>>({})
  const [renaming, setRenaming] = useState(false)
  const [newHandle, setNewHandle] = useState('')
  const [darkModeEnabled, setDarkModeEnabled] = useState(() => isDarkMode())
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const containerRef = useRef<HTMLDivElement>(null)
  const menuRef = useRef<HTMLDivElement>(null)
  const buttonRef = useRef<HTMLButtonElement>(null)

  const setMenuOpen = (next: boolean) => {
    setOpen(next)
    onOpenChange?.(next)
  }

  useLayoutEffect(() => {
    if (!open || !buttonRef.current) {
      setMenuStyle({})
      return
    }
    const update = () => {
      const rect = buttonRef.current!.getBoundingClientRect()
      const vw = window.innerWidth
      const margin = 16
      const menuWidth = Math.min(360, vw - margin * 2)
      const isMobile = vw < 720
      const top = rect.bottom + 8

      setMenuFillHeight(isMobile)

      if (isMobile) {
        setMenuStyle({
          position: 'fixed',
          top,
          left: margin,
          right: margin,
          bottom: 12,
          width: 'auto',
          zIndex: menuZIndex,
        })
      } else {
        setMenuStyle({
          position: 'fixed',
          top,
          right: Math.max(margin, vw - rect.right),
          width: menuWidth,
          maxHeight: `calc(100svh - ${top + 12}px)`,
          zIndex: menuZIndex,
        })
      }
    }
    update()
    window.addEventListener('resize', update)
    return () => window.removeEventListener('resize', update)
  }, [open, menuZIndex])

  const userId = profile?.userId

  useEffect(() => {
    if (!open || !userId || matches !== null) return
    void (async () => {
      try {
        const history = await fetchMatchHistory(userId)
        setMatches(history)
        const disputed = history.filter((m) => m.status === 'DISPUTED')
        if (disputed.length) {
          const pairs = await Promise.all(
            disputed.map(async (m) => [m.id, await fetchDispute(m.id)] as const),
          )
          setDisputes(Object.fromEntries(pairs))
        }
        const opponents = [
          ...new Set(
            history.map((m) =>
              m.defenderUserId === userId ? m.challengerUserId : m.defenderUserId,
            ),
          ),
        ]
        if (opponents.length) setOpponentHandles(await fetchHandles(opponents))
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Failed to load match history')
        setMatches([])
      }
    })()
  }, [open, userId, matches])

  useEffect(() => {
    const keepsAccountOpen = (target: EventTarget | null) => {
      if (!(target instanceof Element)) return false
      return Boolean(
        target.closest('.robo-caddy') ||
          target.closest('.map-header') ||
          target.closest('.court-pin') ||
          target.closest('.panel--profile') ||
          target.closest('.panel--match') ||
          target.closest('.panel--court') ||
          target.closest('.panel--challenges') ||
          target.closest('.overlay-backdrop'),
      )
    }

    const onPointerDown = (e: PointerEvent) => {
      const target = e.target as Node
      if (
        !containerRef.current?.contains(target) &&
        !menuRef.current?.contains(target) &&
        !keepsAccountOpen(target)
      ) {
        setMenuOpen(false)
      }
    }
    document.addEventListener('pointerdown', onPointerDown)
    return () => document.removeEventListener('pointerdown', onPointerDown)
  }, [])

  const submitRename = async () => {
    if (!profile) return
    setBusy(true)
    setError(null)
    try {
      onProfileUpdated(await changeHandle(profile, newHandle.trim()))
      setRenaming(false)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Rename failed')
    } finally {
      setBusy(false)
    }
  }

  const availability = profile ? handleChangeAvailability(profile) : null

  const menu = open && profile && (
    <div
      ref={menuRef}
      className={`profile-dropdown__menu${menuFillHeight ? ' profile-dropdown__menu--fill' : ''}`}
      style={menuStyle}
    >
      <ProfileCard
        profile={profile}
        matches={matches}
        disputes={disputes}
        opponentHandles={opponentHandles}
        showMatchHistory={false}
        onViewProfile={onViewProfile}
        onOpenMatch={onOpenMatch}
      />

      <div className="profile-dropdown__account">
        {!renaming ? (
          <>
            {availability?.allowed ? (
              <button
                type="button"
                className="btn btn--ghost btn--small"
                onClick={() => {
                  setRenaming(true)
                  setNewHandle(profile.handle)
                  setError(null)
                }}
              >
                Change handle
              </button>
            ) : (
              <p className="panel__meta">
                Next rename available{' '}
                {availability?.nextChangeAt?.toLocaleDateString(undefined, {
                  month: 'long',
                  day: 'numeric',
                })}
              </p>
            )}
            {!profile.handleChangedAt && (
              <p className="panel__meta">
                One free rename, then once every 90 days.
              </p>
            )}
          </>
        ) : (
          <form
            className="gauntlet-form"
            onSubmit={(e) => {
              e.preventDefault()
              void submitRename()
            }}
          >
            <label>
              New handle
              <input
                type="text"
                value={newHandle}
                onChange={(e) => setNewHandle(e.target.value)}
                maxLength={24}
                required
              />
            </label>
            <div className="gauntlet-form__actions">
              <button type="submit" className="btn btn--primary btn--small" disabled={busy}>
                {busy ? 'Saving…' : 'Save'}
              </button>
              <button
                type="button"
                className="btn btn--ghost btn--small"
                onClick={() => setRenaming(false)}
                disabled={busy}
              >
                Cancel
              </button>
            </div>
          </form>
        )}
        {error && <p className="form-error">{error}</p>}

        {onRoboCaddyPrefChange && (
          <label className="gauntlet-form__checkbox profile-dropdown__toggle">
            <input
              type="checkbox"
              checked={roboCaddyEnabled}
              onChange={(e) => onRoboCaddyPrefChange(e.target.checked)}
            />
            Robo Caddy tips
          </label>
        )}

        {isAdmin && onAdminUiPrefChange && (
          <label className="gauntlet-form__checkbox profile-dropdown__toggle">
            <input
              type="checkbox"
              checked={adminUiEnabled}
              onChange={(e) => onAdminUiPrefChange(e.target.checked)}
            />
            Admin tools
          </label>
        )}

        <label className="gauntlet-form__checkbox profile-dropdown__toggle">
          <input
            type="checkbox"
            checked={darkModeEnabled}
            onChange={(e) => {
              const enabled = e.target.checked
              setDarkModeEnabled(enabled)
              setDarkMode(enabled)
            }}
          />
          Dark mode
        </label>

        <button type="button" className="btn btn--ghost btn--small" onClick={signOut}>
          Sign out
        </button>
      </div>

      <div className="profile-dropdown__history">
        <ProfileMatchHistory
          matches={matches}
          disputes={disputes}
          opponentHandles={opponentHandles}
          profile={profile}
          onViewProfile={onViewProfile}
          onOpenMatch={onOpenMatch}
        />
      </div>
    </div>
  )

  return (
    <div className="profile-dropdown" ref={containerRef}>
      <button
        ref={buttonRef}
        type="button"
        className={
          compact
            ? `map-util-btn map-util-btn--profile${open ? ' map-util-btn--toggled' : ''}`
            : `btn btn--ghost${open ? ' btn--toggled' : ''}`
        }
        disabled={!profile}
        onClick={() => setMenuOpen(!open)}
        aria-label={profile ? `Profile: ${profile.handle}` : 'Profile'}
        title={profile?.handle}
      >
        {compact ? (
          <>
            <ProfileIcon />
            <span className="map-util-btn__profile-handle">
              {profile?.handle?.[0]?.toUpperCase() ?? '…'}
            </span>
          </>
        ) : (
          <>{profile ? `${profile.handle} ▾` : '…'}</>
        )}
      </button>

      {menu && createPortal(menu, portalRoot?.current ?? document.body)}
    </div>
  )
}
