import { Fragment, useCallback, useEffect, useMemo, useRef, useState, useSyncExternalStore } from 'react'
import Map, {
  GeolocateControl,
  Marker,
  NavigationControl,
  type MapRef,
} from 'react-map-gl/maplibre'
import type { MapLayerMouseEvent } from 'maplibre-gl'
import { useAuthenticator } from '@aws-amplify/ui-react'
import 'maplibre-gl/dist/maplibre-gl.css'
import {
  ensureMyProfile,
  fetchActiveGauntlets,
  fetchAllCourts,
  fetchHandles,
  fetchMatch,
  fetchProfileByUserId,
  syncPendingMatchRatings,
  updateHomeArea,
  type Court,
  type Gauntlet,
  type PlayerProfile,
} from '../lib/data'
import { useMapPanelAnchor, focusPinForPanels } from '../lib/mapPanelAnchor'
import { CourtPanel } from './CourtPanel'
import { AddCourtPanel } from './AddCourtPanel'
import { ChallengesPanel } from './ChallengesPanel'
import { ProfileDropdown } from './ProfileDropdown'
import { ProfilePanel } from './ProfilePanel'
import { MatchModal } from './MatchModal'
import { RequestSentToast } from './RequestSentToast'
import { RoboCaddy } from './RoboCaddy'
import { HomeMarker } from './HomeMarker'
import { FocusPin } from './FocusPin'
import { CourtMarker } from './CourtMarker'
import { PanelLayer } from './PanelLayer'
import { AddCourtIcon, RequestsIcon } from './MapIcons'
import { SearchBar } from './SearchBar'
import type { GeocodeResult } from '../lib/geocode'
import { homeAreaFromProfile } from '../lib/homeArea'
import { homeAreaMapBounds, resolveHomeAreaFromPin } from '../lib/homeRegion'
import type { HomeArea } from '../lib/homeRegion'
import { usePanelStack } from '../lib/panelStack'
import {
  markMatchViewed,
} from '../lib/seenState'
import { primeRequestNotificationAudio, unviewedRequestsNotificationKeys, markRequestsNotificationsSeen } from '../lib/requestsNotifications'
import { useRequestsLiveUpdates } from '../lib/useRequestsLiveUpdates'
import { isRoboCaddyEnabled, setRoboCaddyEnabled } from '../lib/roboCaddyPrefs'
import { isAdminUiEnabled, setAdminUiEnabled } from '../lib/adminUiPrefs'
import { fetchIsAdmin } from '../lib/admin'
import type { RoboCaddyScene } from '../lib/roboCaddyAdvice'
import { resolveMapStyle } from '../lib/mapStyles'
import { getThemeMode, subscribeThemeMode, type ThemeMode } from '../lib/themePrefs'

// Waterloo Region, Ontario: covers Kitchener–Waterloo–Cambridge
const INITIAL_VIEW = { longitude: -80.49, latitude: 43.44, zoom: 11 }
/** Pin height used to park the separate title marker above the icon. */
const COURT_PIN_HEIGHT_PX = 43

/** Keep city zoom (~11+) full size; ease down toward country zoom (~4). */
function courtMarkerScaleForZoom(zoom: number): number {
  const cityZoom = 11
  const countryZoom = 4
  const minScale = 0.55
  if (zoom >= cityZoom) return 1
  if (zoom <= countryZoom) return minScale
  return minScale + ((zoom - countryZoom) / (cityZoom - countryZoom)) * (1 - minScale)
}

export function MapScreen() {
  const { user, signOut } = useAuthenticator((ctx) => [ctx.user])
  const mapRef = useRef<MapRef>(null)
  const mapScreenRef = useRef<HTMLDivElement>(null)
  const requestsButtonRef = useRef<HTMLButtonElement>(null)
  const homeAreaFlownRef = useRef(false)

  const [courts, setCourts] = useState<Record<string, Court>>({})
  const [gauntlets, setGauntlets] = useState<Gauntlet[]>([])
  const [handles, setHandles] = useState<Record<string, string>>({})
  const [myProfile, setMyProfile] = useState<PlayerProfile | null>(null)
  const [viewProfileUserId, setViewProfileUserId] = useState<string | null>(null)
  const [openMatchId, setOpenMatchId] = useState<string | null>(null)
  const [matchCourtId, setMatchCourtId] = useState<string | null>(null)
  const [courtMatchRefresh, setCourtMatchRefresh] = useState(0)
  const [selectedCourtId, setSelectedCourtId] = useState<string | null>(null)
  const [showChallenges, setShowChallenges] = useState(false)
  const [addingCourt, setAddingCourt] = useState(false)
  const [draftCourtPos, setDraftCourtPos] = useState<{ lat: number; lng: number } | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [seenVersion, setSeenVersion] = useState(0)
  const [roboCaddyEnabled, setRoboCaddyEnabledState] = useState(() =>
    isRoboCaddyEnabled(user.userId),
  )
  const [isAdmin, setIsAdmin] = useState(false)
  const [adminUiEnabled, setAdminUiEnabledState] = useState(() =>
    isAdminUiEnabled(user.userId),
  )
  const [requestSentToast, setRequestSentToast] = useState(false)
  const [mapReady, setMapReady] = useState(false)
  const [mapZoom, setMapZoom] = useState(INITIAL_VIEW.zoom)
  const [placingHomePin, setPlacingHomePin] = useState(false)
  const [placingHomePinBusy, setPlacingHomePinBusy] = useState(false)
  const [homePopoverOpen, setHomePopoverOpen] = useState(false)
  const [homePinDrag, setHomePinDrag] = useState<{ lat: number; lng: number } | null>(null)
  const [homePinDragging, setHomePinDragging] = useState(false)
  const [savingHomeDrag, setSavingHomeDrag] = useState(false)
  const showAdminUi = isAdmin && adminUiEnabled
  const themeMode = useSyncExternalStore(
    subscribeThemeMode,
    getThemeMode,
    (): ThemeMode => 'light',
  )
  const mapSelection = useMemo(() => resolveMapStyle(themeMode), [themeMode])
  const mapStyle = mapSelection.style
  const courtMarkerScale = useMemo(() => courtMarkerScaleForZoom(mapZoom), [mapZoom])
  const courtMarkerZoomStyle = useMemo(
    () =>
      ({
        transform: `scale(${courtMarkerScale})`,
        transformOrigin: 'bottom center',
      }) as const,
    [courtMarkerScale],
  )
  const { focus, blur, zIndex, stack } = usePanelStack()
  const { challenges, ongoingMatches, disputes, refresh: refreshRequests } =
    useRequestsLiveUpdates(user.userId)

  const gauntletsByCourt = useMemo(() => {
    const grouped: Record<string, Gauntlet[]> = {}
    for (const g of gauntlets) {
      ;(grouped[g.courtId] ??= []).push(g)
    }
    return grouped
  }, [gauntlets])

  const matchCountByCourt = useMemo(() => {
    const counts: Record<string, number> = {}
    for (const m of ongoingMatches) {
      if (!m.courtId) continue
      counts[m.courtId] = (counts[m.courtId] ?? 0) + 1
    }
    return counts
  }, [ongoingMatches])

  const reloadGauntlets = useCallback(async () => {
    try {
      const active = await fetchActiveGauntlets()
      setGauntlets(active)
      const owners = [...new Set(active.map((g) => g.ownerUserId))]
      if (owners.length) setHandles(await fetchHandles(owners))
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load gauntlets')
    }
  }, [])

  const reloadCourts = useCallback(async () => {
    try {
      const all = await fetchAllCourts()
      setCourts(Object.fromEntries(all.map((c) => [c.id, c])))
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load courts')
    }
  }, [])

  useEffect(() => {
    // False positive: state is set after the fetches resolve, not synchronously.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void reloadGauntlets()
    void reloadCourts()
  }, [reloadGauntlets, reloadCourts])

  useEffect(() => {
    void fetchIsAdmin().then(setIsAdmin)
  }, [user.userId])

  useEffect(() => {
    setAdminUiEnabledState(isAdminUiEnabled(user.userId))
  }, [user.userId])

  useEffect(() => {
    const unlock = () => primeRequestNotificationAudio()
    document.addEventListener('pointerdown', unlock, { once: true })
    return () => document.removeEventListener('pointerdown', unlock)
  }, [])

  // First sign-in bootstrap: create a profile with a generated handle
  useEffect(() => {
    void ensureMyProfile(user.userId)
      .then(async (profile) => {
        setMyProfile(profile)
        await reloadGauntlets()
      })
      .catch((e: unknown) =>
        setError(e instanceof Error ? e.message : 'Failed to load your profile'),
      )
    void syncPendingMatchRatings(user.userId).then(() =>
      fetchProfileByUserId(user.userId).then((p) => {
        if (p) {
          setMyProfile(p)
          void reloadGauntlets()
        }
      }),
    )
  }, [user.userId, reloadGauntlets])

  useEffect(() => {
    homeAreaFlownRef.current = false
  }, [user.userId])

  const requestsBadgeCount = useMemo(() => {
    void seenVersion
    return unviewedRequestsNotificationKeys(
      challenges,
      ongoingMatches,
      disputes,
      user.userId,
    ).size
  }, [challenges, ongoingMatches, disputes, user.userId, seenVersion])

  const flyToPlace = useCallback((place: GeocodeResult) => {
    const map = mapRef.current
    if (!map) return
    if (place.bbox) {
      const [south, north, west, east] = place.bbox
      map.fitBounds(
        [
          [west, south],
          [east, north],
        ],
        { padding: 40, maxZoom: 15, duration: 1500 },
      )
    } else {
      map.flyTo({ center: [place.lng, place.lat], zoom: 13, duration: 1500 })
    }
  }, [])

  const flyToHomePin = useCallback((lat: number, lng: number) => {
    const map = mapRef.current
    if (!map) return
    map.flyTo({ center: [lng, lat], duration: 800 })
  }, [])

  const flyToHomeArea = useCallback((area: HomeArea) => {
    const map = mapRef.current
    if (!map) return
    const [south, north, west, east] = homeAreaMapBounds(area)
    map.fitBounds(
      [
        [west, south],
        [east, north],
      ],
      { padding: 40, maxZoom: 13, duration: 1500 },
    )
  }, [])

  const homeArea = useMemo(
    () => (myProfile ? homeAreaFromProfile(myProfile) : null),
    [myProfile],
  )

  useEffect(() => {
    if (!mapReady || !homeArea || homeAreaFlownRef.current) return
    homeAreaFlownRef.current = true
    flyToHomeArea(homeArea)
  }, [mapReady, homeArea, flyToHomeArea])

  const startPlacingHomePin = useCallback(() => {
    setHomePopoverOpen(false)
    setSelectedCourtId(null)
    blur('court')
    setPlacingHomePin(true)
    setAddingCourt(false)
    setDraftCourtPos(null)
    blur('addCourt')
  }, [blur])

  const cancelPlacingHomePin = useCallback(() => {
    setPlacingHomePin(false)
    setPlacingHomePinBusy(false)
    setHomePinDrag(null)
    setHomePinDragging(false)
  }, [])

  const pinDisplay = homePinDrag ?? (homeArea ? { lat: homeArea.lat, lng: homeArea.lng } : null)

  const handleHomePinDragEnd = useCallback(
    (lat: number, lng: number) => {
      if (!myProfile) return
      setHomePinDragging(false)
      setHomePinDrag({ lat, lng })
      setSavingHomeDrag(true)
      void (async () => {
        try {
          const area = await resolveHomeAreaFromPin(lat, lng)
          const updated = await updateHomeArea(myProfile, area)
          setMyProfile(updated)
        } catch (err) {
          setError(err instanceof Error ? err.message : 'Failed to move home pin')
        } finally {
          setSavingHomeDrag(false)
          setHomePinDrag(null)
        }
      })()
    },
    [myProfile],
  )

  const selectedCourt = selectedCourtId ? courts[selectedCourtId] : null
  const anchorCourt =
    selectedCourt ?? (matchCourtId ? courts[matchCourtId] ?? null : null)
  const matchPanelOpen = Boolean(openMatchId)
  const courtPanelOpen = Boolean(selectedCourt && !matchPanelOpen)
  const anchorEnabled = Boolean(anchorCourt && (courtPanelOpen || matchPanelOpen))
  const { layout: panelLayout, recenterPin } = useMapPanelAnchor(
    mapRef,
    anchorCourt,
    anchorEnabled,
    { courtOpen: courtPanelOpen, matchOpen: matchPanelOpen },
  )

  useEffect(() => {
    if (anchorEnabled) recenterPin()
  }, [anchorEnabled, selectedCourtId, openMatchId, recenterPin])

  const roboCaddyScene = useMemo((): RoboCaddyScene => {
    if (openMatchId) return 'match'

    const courtIdx = stack.indexOf('court')
    const challengesIdx = stack.indexOf('challenges')
    const courtOpen = courtPanelOpen && courtIdx !== -1
    const challengesOpen = showChallenges && challengesIdx !== -1

    if (courtOpen && challengesOpen) {
      return challengesIdx > courtIdx ? 'challenges' : 'court'
    }
    if (challengesOpen) return 'challenges'
    if (courtOpen) return 'court'
    return 'map'
  }, [openMatchId, courtPanelOpen, showChallenges, stack])

  const handleRoboCaddyPrefChange = useCallback(
    (enabled: boolean) => {
      setRoboCaddyEnabled(user.userId, enabled)
      setRoboCaddyEnabledState(enabled)
    },
    [user.userId],
  )

  const handleAdminUiPrefChange = useCallback(
    (enabled: boolean) => {
      setAdminUiEnabled(user.userId, enabled)
      setAdminUiEnabledState(enabled)
    },
    [user.userId],
  )

  const bumpMatchSeen = useCallback(() => setSeenVersion((v) => v + 1), [])

  const handleChallengeSent = useCallback(async () => {
    await refreshRequests()
    if (!showChallenges) setRequestSentToast(true)
  }, [refreshRequests, showChallenges])

  const dismissRequestSentToast = useCallback(() => setRequestSentToast(false), [])

  const openRequestsFromToast = useCallback(() => {
    setRequestSentToast(false)
    setShowChallenges(true)
    setAddingCourt(false)
    setDraftCourtPos(null)
    markRequestsNotificationsSeen(
      challenges,
      ongoingMatches,
      disputes,
      user.userId,
    )
    setSeenVersion((n) => n + 1)
    void refreshRequests()
    focus('challenges')
  }, [challenges, disputes, focus, ongoingMatches, refreshRequests, user.userId])

  const handleAccountOpenChange = useCallback(
    (open: boolean) => {
      if (open) focus('account')
      else blur('account')
    },
    [blur, focus],
  )

  const openProfile = useCallback(
    (userId: string) => {
      setViewProfileUserId(userId)
      focus('profile')
    },
    [focus],
  )

  const closeProfile = useCallback(() => {
    setViewProfileUserId(null)
    blur('profile')
  }, [blur])

  const openMatch = useCallback(
    (matchId: string, options?: { keepProfile?: boolean }) => {
      if (!options?.keepProfile) {
        setViewProfileUserId(null)
        blur('profile')
      }
      void fetchMatch(matchId).then((m) => {
        if (!m) return
        const sameCourtOpen = Boolean(
          m.courtId && selectedCourtId && selectedCourtId === m.courtId,
        )

        setOpenMatchId(matchId)
        if (m.courtId) {
          setMatchCourtId(m.courtId)
        }

        if (sameCourtOpen) {
          // Keep selectedCourtId so court panel restores when match closes.
        } else {
          setSelectedCourtId(null)
          blur('court')
        }

        if (m.courtId) {
          const court = courts[m.courtId]
          if (court && mapRef.current) {
            focusPinForPanels(mapRef.current.getMap(), court)
          }
        }
        focus('match')
      })
    },
    [blur, courts, focus, selectedCourtId],
  )

  const closeMatch = useCallback(() => {
    if (openMatchId) {
      markMatchViewed(user.userId, openMatchId)
      setSeenVersion((v) => v + 1)
    }
    setOpenMatchId(null)
    setMatchCourtId(null)
    blur('match')
  }, [blur, openMatchId, user.userId])

  const openCourt = useCallback(
    (courtId: string, options?: { keepChallenges?: boolean }) => {
      setHomePopoverOpen(false)
      setSelectedCourtId(courtId)
      if (!options?.keepChallenges) {
        setShowChallenges(false)
      }
      setAddingCourt(false)
      setDraftCourtPos(null)
      const court = courts[courtId]
      if (court && mapRef.current) {
        focusPinForPanels(mapRef.current.getMap(), court)
      }
      focus('court')
      if (openMatchId && matchCourtId === courtId) {
        focus('match')
      }
    },
    [courts, focus, matchCourtId, openMatchId],
  )

  const closeCourt = useCallback(() => {
    setSelectedCourtId(null)
    blur('court')
  }, [blur])

  const handleCourtPinClick = useCallback(
    (courtId: string) => {
      closeProfile()
      if (openMatchId && matchCourtId === courtId) {
        setSelectedCourtId(null)
        blur('court')
        const court = courts[courtId]
        if (court && mapRef.current) {
          focusPinForPanels(mapRef.current.getMap(), court)
        }
        focus('match')
        return
      }
      if (openMatchId && matchCourtId !== courtId) {
        closeMatch()
      }
      openCourt(courtId, { keepChallenges: showChallenges })
    },
    [blur, closeMatch, closeProfile, courts, focus, matchCourtId, openCourt, openMatchId, showChallenges],
  )

  const focusPinCourtId = matchPanelOpen ? matchCourtId : null

  const toggleChallenges = useCallback(() => {
    setRequestSentToast(false)
    setShowChallenges((v) => {
      const next = !v
      if (next) {
        setAddingCourt(false)
        setDraftCourtPos(null)
        markRequestsNotificationsSeen(
          challenges,
          ongoingMatches,
          disputes,
          user.userId,
        )
        setSeenVersion((n) => n + 1)
        void refreshRequests()
        focus('challenges')
      } else {
        blur('challenges')
      }
      return next
    })
  }, [blur, challenges, disputes, focus, ongoingMatches, refreshRequests, user.userId])

  const handleMapClick = useCallback(
    (e: MapLayerMouseEvent) => {
      if (placingHomePin) {
        if (!myProfile || placingHomePinBusy) return
        setPlacingHomePinBusy(true)
        void (async () => {
          try {
            const area = await resolveHomeAreaFromPin(e.lngLat.lat, e.lngLat.lng)
            const updated = await updateHomeArea(myProfile, area)
            setMyProfile(updated)
            setPlacingHomePin(false)
            flyToHomeArea(area)
          } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to save home pin')
          } finally {
            setPlacingHomePinBusy(false)
          }
        })()
        return
      }
      if (addingCourt) {
        setDraftCourtPos({ lat: e.lngLat.lat, lng: e.lngLat.lng })
      } else {
        setHomePopoverOpen(false)
        if (openMatchId) closeMatch()
        closeCourt()
      }
    },
    [
      addingCourt,
      closeCourt,
      closeMatch,
      flyToHomeArea,
      myProfile,
      openMatchId,
      placingHomePin,
      placingHomePinBusy,
    ],
  )

  const handleCourtCreated = useCallback(
    (court: Court) => {
      setCourts((prev) => ({ ...prev, [court.id]: court }))
      setAddingCourt(false)
      setDraftCourtPos(null)
      blur('addCourt')
      openCourt(court.id)
    },
    [blur, openCourt],
  )

  return (
    <div
      className={`map-screen map-screen--basemap-${mapSelection.basemapId}`}
      ref={mapScreenRef}
    >
      <Map
        ref={mapRef}
        initialViewState={INITIAL_VIEW}
        mapStyle={mapStyle}
        onLoad={() => setMapReady(true)}
        onMove={(e) => {
          const next = Math.round(e.viewState.zoom * 10) / 10
          setMapZoom((prev) => (prev === next ? prev : next))
        }}
        onError={(e) => {
          console.error('[map] failed to load basemap', e.error)
          setError(
            'Map tiles failed to load — check your Mapbox token in .env.local and restart the dev server.',
          )
        }}
        onClick={handleMapClick}
        cursor={addingCourt || placingHomePin ? 'crosshair' : 'grab'}
      >
        <NavigationControl position="bottom-right" />
        <GeolocateControl position="bottom-right" trackUserLocation={false} />

        {pinDisplay && !placingHomePin && (
          <Marker
            longitude={pinDisplay.lng}
            latitude={pinDisplay.lat}
            anchor="bottom"
            draggable
            onClick={(e) => {
              e.originalEvent.stopPropagation()
              closeCourt()
              flyToHomePin(pinDisplay.lat, pinDisplay.lng)
              setHomePopoverOpen((open) => !open)
            }}
            onDragStart={() => {
              setHomePopoverOpen(false)
              setHomePinDragging(true)
              closeCourt()
            }}
            onDrag={(e) =>
              setHomePinDrag({ lat: e.lngLat.lat, lng: e.lngLat.lng })
            }
            onDragEnd={(e) => handleHomePinDragEnd(e.lngLat.lat, e.lngLat.lng)}
          >
            <HomeMarker
              regionLabel={homeArea?.regionLabel ?? 'Home'}
              userId={user.userId}
              globalElo={myProfile?.globalElo ?? 1200}
              popoverOpen={homePopoverOpen}
              dragging={homePinDragging}
              saving={savingHomeDrag}
            />
          </Marker>
        )}

        {Object.values(courts).map((court) => {
          const openCount = gauntletsByCourt[court.id]?.length ?? 0
          const matchCount = matchCountByCourt[court.id] ?? 0
          const selected = court.id === selectedCourtId
          const underOverlay = court.id === focusPinCourtId
          const active = matchCount > 0 || openCount > 0
          const onMarkerClick = (e: { originalEvent: { stopPropagation: () => void } }) => {
            e.originalEvent.stopPropagation()
            handleCourtPinClick(court.id)
          }
          // Separate pin/label markers so titles stack above other pins; focused court tops all.
          const pinZ = selected ? 20 : 1
          const labelZ = selected ? 21 : 2
          const hasLabel = Boolean(court.name?.trim())
          const labelOffsetY = -Math.round(COURT_PIN_HEIGHT_PX * courtMarkerScale)
          return (
            <Fragment key={court.id}>
              <Marker
                longitude={court.lng}
                latitude={court.lat}
                anchor="bottom"
                style={{ zIndex: pinZ }}
                onClick={onMarkerClick}
              >
                <div className="court-marker-zoom" style={courtMarkerZoomStyle}>
                  <CourtMarker
                    court={court}
                    part="pin"
                    matchCount={matchCount}
                    openCount={openCount}
                    active={active}
                    selected={selected}
                    underOverlay={underOverlay}
                  />
                </div>
              </Marker>
              {hasLabel && (
                <Marker
                  longitude={court.lng}
                  latitude={court.lat}
                  anchor="bottom"
                  offset={[0, labelOffsetY]}
                  style={{ zIndex: labelZ }}
                  onClick={onMarkerClick}
                >
                  <div className="court-marker-zoom" style={courtMarkerZoomStyle}>
                    <CourtMarker
                      court={court}
                      part="label"
                      selected={selected}
                      underOverlay={underOverlay}
                    />
                  </div>
                </Marker>
              )}
            </Fragment>
          )
        })}

        {draftCourtPos && (
          <Marker longitude={draftCourtPos.lng} latitude={draftCourtPos.lat} anchor="bottom">
            <div className="court-marker-zoom" style={courtMarkerZoomStyle}>
              <CourtMarker court={{ name: 'New court' }} draft />
            </div>
          </Marker>
        )}
      </Map>

      <header className="map-header">
        <div className="map-header__toolbar">
          <SearchBar compact onSelect={flyToPlace} />
          <button
            ref={requestsButtonRef}
            type="button"
            className={`map-util-btn map-util-btn--badged${showChallenges ? ' map-util-btn--toggled' : ''}${requestSentToast ? ' map-util-btn--hint' : ''}`}
            aria-label="Requests"
            onClick={() => toggleChallenges()}
          >
            <span className="map-util-btn__icon">
              <RequestsIcon />
            </span>
            <span className="map-util-btn__label">Requests</span>
            {requestsBadgeCount > 0 && (
              <span className="map-util-btn__badge">{requestsBadgeCount}</span>
            )}
          </button>
          <button
            type="button"
            className={`map-util-btn${addingCourt ? ' map-util-btn--toggled' : ''}`}
            aria-label={addingCourt ? 'Cancel add court' : 'Add court'}
            title={addingCourt ? 'Cancel' : 'Add court'}
            onClick={() => {
              setAddingCourt((v) => {
                const next = !v
                if (next) {
                  setDraftCourtPos(null)
                  closeCourt()
                  setShowChallenges(false)
                  blur('challenges')
                  focus('addCourt')
                } else {
                  blur('addCourt')
                }
                return next
              })
            }}
          >
            <AddCourtIcon />
          </button>
          <ProfileDropdown
            compact
            profile={myProfile}
            onProfileUpdated={setMyProfile}
            onViewProfile={openProfile}
            onOpenMatch={openMatch}
            signOut={signOut}
            menuZIndex={zIndex('account')}
            portalRoot={mapScreenRef}
            onOpenChange={handleAccountOpenChange}
            roboCaddyEnabled={roboCaddyEnabled}
            onRoboCaddyPrefChange={handleRoboCaddyPrefChange}
            isAdmin={isAdmin}
            adminUiEnabled={adminUiEnabled}
            onAdminUiPrefChange={handleAdminUiPrefChange}
            placingHomePin={placingHomePin}
            placingHomePinBusy={placingHomePinBusy}
            onStartPlacingHomePin={startPlacingHomePin}
          />
        </div>
      </header>

      {addingCourt && !draftCourtPos && (
        <div className="map-notice">Tap the map where the court is</div>
      )}
      {placingHomePin && (
        <div className="map-notice" onClick={cancelPlacingHomePin}>
          {placingHomePinBusy
            ? 'Saving home pin…'
            : 'Tap the map to set your home pin (tap here to cancel)'}
        </div>
      )}
      {error && (
        <div className="map-notice map-notice--error" onClick={() => setError(null)}>
          {error} (tap to dismiss)
        </div>
      )}

      {requestSentToast && !showChallenges && (
        <RequestSentToast
          anchorRef={requestsButtonRef}
          onDismiss={dismissRequestSentToast}
          onOpenRequests={openRequestsFromToast}
        />
      )}

      {openMatchId && (
        <PanelLayer zIndex={zIndex('match')} elevated onBackdropClick={closeMatch}>
          {anchorCourt && panelLayout?.pin && (
            <FocusPin
              court={anchorCourt}
              x={panelLayout.pin.x}
              bottom={panelLayout.pin.bottom}
              matchCount={matchCountByCourt[anchorCourt.id] ?? 0}
              active={
                (matchCountByCourt[anchorCourt.id] ?? 0) > 0 ||
                (gauntletsByCourt[anchorCourt.id]?.length ?? 0) > 0
              }
              openCount={gauntletsByCourt[anchorCourt.id]?.length ?? 0}
              label={anchorCourt.name}
              scale={courtMarkerScale}
              onClick={() => handleCourtPinClick(anchorCourt.id)}
            />
          )}
          <MatchModal
            matchId={openMatchId}
            currentUserId={user.userId}
            isAdmin={showAdminUi}
            courts={courts}
            panelStyle={panelLayout?.match}
            onClose={closeMatch}
            onChanged={() => {
              void refreshRequests()
              void reloadGauntlets()
              setCourtMatchRefresh((n) => n + 1)
              void syncPendingMatchRatings(user.userId).then(() =>
                fetchProfileByUserId(user.userId).then((p) => {
                  if (p) setMyProfile(p)
                }),
              )
            }}
            onViewProfile={openProfile}
          />
        </PanelLayer>
      )}

      {viewProfileUserId && (
        <PanelLayer zIndex={zIndex('profile')} modal onBackdropClick={closeProfile}>
          <ProfilePanel
            userId={viewProfileUserId}
            isAdmin={showAdminUi}
            popup
            onClose={closeProfile}
            onViewProfile={openProfile}
            onOpenMatch={(matchId) => openMatch(matchId, { keepProfile: true })}
          />
        </PanelLayer>
      )}

      {selectedCourt && !matchPanelOpen && (
        <PanelLayer zIndex={zIndex('court')}>
          <CourtPanel
            key={selectedCourt.id}
            court={selectedCourt}
            gauntlets={gauntletsByCourt[selectedCourt.id] ?? []}
            challenges={challenges}
            handles={handles}
            currentUserId={user.userId}
            matchRefreshKey={courtMatchRefresh}
            panelStyle={panelLayout?.court}
            onClose={closeCourt}
            onGauntletDropped={reloadGauntlets}
            onChallengeSent={handleChallengeSent}
            onViewProfile={openProfile}
            onOpenMatch={openMatch}
            matchSeenVersion={seenVersion}
            onMatchesSeen={bumpMatchSeen}
            isAdmin={showAdminUi}
            onCourtDeleted={() => {
              closeCourt()
              void reloadCourts()
              void reloadGauntlets()
            }}
          />
        </PanelLayer>
      )}

      {showChallenges && (
        <PanelLayer zIndex={zIndex('challenges')}>
          <ChallengesPanel
            challenges={challenges}
            courts={courts}
            currentUserId={user.userId}
            onClose={() => {
              setShowChallenges(false)
              blur('challenges')
            }}
            onChanged={() => {
              void refreshRequests()
              void reloadGauntlets()
            }}
            onViewProfile={openProfile}
            onOpenMatch={openMatch}
            onOpenCourt={handleCourtPinClick}
            matchSeenVersion={seenVersion}
            challengeSeenVersion={seenVersion}
            onMatchesSeen={bumpMatchSeen}
          />
        </PanelLayer>
      )}

      {addingCourt && draftCourtPos && (
        <PanelLayer zIndex={zIndex('addCourt')}>
          <AddCourtPanel
            position={draftCourtPos}
            onClose={() => {
              setAddingCourt(false)
              setDraftCourtPos(null)
              blur('addCourt')
            }}
            onCreated={handleCourtCreated}
          />
        </PanelLayer>
      )}

      <RoboCaddy
        enabled={roboCaddyEnabled}
        userId={user.userId}
        scene={roboCaddyScene}
        matchId={openMatchId}
        court={matchCourtId ? courts[matchCourtId] ?? null : null}
      />
    </div>
  )
}
