import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import Map, {
  GeolocateControl,
  Marker,
  NavigationControl,
  type MapRef,
} from 'react-map-gl/maplibre'
import type { StyleSpecification } from 'maplibre-gl'
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
import { RoboCaddy } from './RoboCaddy'
import { FocusPin } from './FocusPin'
import { CourtPin } from './CourtPin'
import { PanelLayer } from './PanelLayer'
import { AddCourtIcon, RequestsIcon } from './MapIcons'
import { SearchBar, type GeocodeResult } from './SearchBar'
import { usePanelStack } from '../lib/panelStack'
import {
  acknowledgeChallenges,
  markMatchViewed,
} from '../lib/seenState'
import { primeRequestNotificationAudio, requestsNotificationKeys } from '../lib/requestsNotifications'
import { useRequestsLiveUpdates } from '../lib/useRequestsLiveUpdates'
import { isRoboCaddyEnabled, setRoboCaddyEnabled } from '../lib/roboCaddyPrefs'
import { isAdminUiEnabled, setAdminUiEnabled } from '../lib/adminUiPrefs'
import { fetchIsAdmin } from '../lib/admin'
import type { RoboCaddyScene } from '../lib/roboCaddyAdvice'

// Waterloo Region, Ontario: covers Kitchener–Waterloo–Cambridge
const INITIAL_VIEW = { longitude: -80.49, latitude: 43.44, zoom: 11 }

// Keyless OSM raster style; swap for a vector style + key later.
const MAP_STYLE: StyleSpecification = {
  version: 8,
  sources: {
    osm: {
      type: 'raster',
      tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
      tileSize: 256,
      attribution: '&copy; OpenStreetMap contributors',
    },
  },
  layers: [{ id: 'osm', type: 'raster', source: 'osm' }],
}

export function MapScreen() {
  const { user, signOut } = useAuthenticator((ctx) => [ctx.user])
  const mapRef = useRef<MapRef>(null)
  const mapScreenRef = useRef<HTMLDivElement>(null)

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
  const showAdminUi = isAdmin && adminUiEnabled
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
      .then(setMyProfile)
      .catch((e: unknown) =>
        setError(e instanceof Error ? e.message : 'Failed to load your profile'),
      )
    void syncPendingMatchRatings(user.userId).then(() =>
      fetchProfileByUserId(user.userId).then((p) => {
        if (p) setMyProfile(p)
      }),
    )
  }, [user.userId])

  const requestsBadgeCount = useMemo(() => {
    void seenVersion
    return requestsNotificationKeys(
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
    setShowChallenges((v) => {
      const next = !v
      if (next) {
        setAddingCourt(false)
        setDraftCourtPos(null)
        const toAck = challenges
          .filter(
            (c) => c.status === 'ACCEPTED' && c.proposedByUserId === user.userId,
          )
          .map((c) => c.id)
        acknowledgeChallenges(user.userId, toAck)
        setSeenVersion((n) => n + 1)
        void refreshRequests()
        focus('challenges')
      } else {
        blur('challenges')
      }
      return next
    })
  }, [blur, challenges, focus, refreshRequests, user.userId])

  const handleMapClick = useCallback(
    (e: MapLayerMouseEvent) => {
      if (addingCourt) {
        setDraftCourtPos({ lat: e.lngLat.lat, lng: e.lngLat.lng })
      } else {
        if (openMatchId) closeMatch()
        closeCourt()
      }
    },
    [addingCourt, closeCourt, closeMatch, openMatchId],
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
    <div className="map-screen" ref={mapScreenRef}>
      <Map
        ref={mapRef}
        initialViewState={INITIAL_VIEW}
        mapStyle={MAP_STYLE}
        onClick={handleMapClick}
        cursor={addingCourt ? 'crosshair' : 'grab'}
      >
        <NavigationControl position="bottom-right" />
        <GeolocateControl position="bottom-right" trackUserLocation={false} />

        {Object.values(courts).map((court) => {
          const openCount = gauntletsByCourt[court.id]?.length ?? 0
          return (
            <Marker
              key={court.id}
              longitude={court.lng}
              latitude={court.lat}
              anchor="bottom"
              onClick={(e) => {
                e.originalEvent.stopPropagation()
                handleCourtPinClick(court.id)
              }}
            >
              <CourtPin
                court={court}
                openCount={openCount}
                active={openCount > 0}
                selected={court.id === selectedCourtId}
                underOverlay={court.id === focusPinCourtId}
              />
            </Marker>
          )
        })}

        {draftCourtPos && (
          <Marker longitude={draftCourtPos.lng} latitude={draftCourtPos.lat} anchor="bottom">
            <CourtPin court={{ name: 'New court' }} draft />
          </Marker>
        )}
      </Map>

      <header className="map-header">
        <div className="map-header__toolbar">
          <SearchBar compact onSelect={flyToPlace} />
          <button
            type="button"
            className={`map-util-btn map-util-btn--badged${showChallenges ? ' map-util-btn--toggled' : ''}`}
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
          />
        </div>
      </header>

      {addingCourt && !draftCourtPos && (
        <div className="map-notice">Tap the map where the court is</div>
      )}
      {error && (
        <div className="map-notice map-notice--error" onClick={() => setError(null)}>
          {error} (tap to dismiss)
        </div>
      )}

      {openMatchId && (
        <PanelLayer zIndex={zIndex('match')} elevated onBackdropClick={closeMatch}>
          {anchorCourt && panelLayout?.pin && (
            <FocusPin
              court={anchorCourt}
              x={panelLayout.pin.x}
              bottom={panelLayout.pin.bottom}
              active={(gauntletsByCourt[anchorCourt.id]?.length ?? 0) > 0}
              openCount={gauntletsByCourt[anchorCourt.id]?.length ?? 0}
              label={anchorCourt.name}
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
            handles={handles}
            currentUserId={user.userId}
            matchRefreshKey={courtMatchRefresh}
            panelStyle={panelLayout?.court}
            onClose={closeCourt}
            onGauntletDropped={reloadGauntlets}
            onChallengeSent={refreshRequests}
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
