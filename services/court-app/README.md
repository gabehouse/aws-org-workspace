# court-app

Map-based tennis matchmaking. Players drop **gauntlets** on courts to broadcast
that they're accepting matches; others challenge, negotiate terms, check in
on-site, play, and report scores. Ratings (Global Elo + Territorial) update
once both sides verify the result.

Stack: React + Vite frontend, AWS Amplify Gen 2 backend (Cognito auth,
AppSync GraphQL API, DynamoDB).

## Backend layout

```
amplify/
  backend.ts          # wires auth + data together
  auth/resource.ts    # Cognito: email login, Admins group
  data/resource.ts    # the full data schema (models below)
```

### Data model

| Model | Purpose |
|---|---|
| `PlayerProfile` | Handle (auto-generated, rename-limited), Elo/territorial ratings, honor score, W/L record |
| `Court` | Fixed court locations with a `geohash` index for map viewport queries |
| `Gauntlet` | An open "I'm accepting matches" flag at a court (no schedule) |
| `Challenge` | The time-nudging negotiation: one proposed time, turns alternate |
| `Match` | The locked contract through check-in, play, and final result |
| `ScoreReport` | Each player's submitted score for a match |
| `Dispute` | Raised when score reports conflict |
| `RatingEvent` | Append-only log of rating changes per player |

Status enums drive the lifecycle:

- `Gauntlet`: `ACTIVE → LOCKED → ARCHIVED` (or `EXPIRED` / `WITHDRAWN`)
- `Challenge`: `PENDING → COUNTERED* → ACCEPTED` (or `DECLINED` / `WITHDRAWN` / `EXPIRED`)

Scheduling is a nudge loop on `Challenge`: `proposedStart` is the single time
on the table and `proposedByUserId` says who put it there. Whoever did *not*
propose it moves next — accept, nudge to a new time (which swaps
`proposedByUserId` and sets status `COUNTERED`), or decline. Accepting creates
a `CONTRACTED` Match at the agreed time and locks the gauntlet.
- `Match`: `CONTRACTED → LIVE → REPORTED → VERIFIED → FINAL`, with
  `DISPUTED`, `VOIDED`, `RAINED_OUT`, `CANCELLED` branches

Two-party records store both players' user ids in a `participants` array
(`ownersDefinedIn`), so either side can act on them. All authenticated users
can read courts, gauntlets, challenges, and matches (the map and challenge
feed are public within the app); only participants can write.

### Geo strategy

No PostGIS needed: gauntlets attach to courts, and courts are a small static
dataset. Each court stores a 5-character geohash; the map queries the geohash
cells covering the viewport via the `geohash` secondary index and filters
precisely client-side.

## Development

From the repo root (pnpm workspace):

```bash
pnpm install
```

Deploy a personal cloud sandbox (needs AWS credentials for your dev account):

```bash
cd services/court-app
npx ampx sandbox --profile <your-dev-profile>
```

This watches `amplify/` and hot-deploys changes, writing `amplify_outputs.json`
(git-ignored) for the frontend to consume.

Run the frontend:

```bash
pnpm dev
```

Typecheck the backend without deploying:

```bash
pnpm typecheck:backend
```

## Deploying through the org

Connect the repo to Amplify Hosting in staging/prod accounts; branch deploys
run `npx ampx pipeline-deploy` automatically. Keep the sandbox in the dev
account.

## Frontend

Screen 0 (the map) is built:

- `src/main.tsx` configures Amplify from `amplify_outputs.json`; `src/App.tsx`
  wraps everything in the Amplify UI `Authenticator`.
- `src/components/MapScreen.tsx` renders a full-screen MapLibre map (keyless
  OSM raster tiles for now), opening on Waterloo Region. Courts are a small
  dataset, so all of them load once at startup and every pin stays visible at
  any zoom; the `geohash` field is still written on create and its index
  remains for viewport-scoped queries if the dataset outgrows this.
- A search bar geocodes cities/postal codes through OSM Nominatim (keyless)
  and flies the map to the selected place.
- Open challenges (backend: `Gauntlet`) show as badges on court pins.
- Tapping a court opens `CourtPanel`: court details, matches at that court,
  open challenges with owner handles, a **Post challenge** form (stakes + note),
  and a **Request** button on others' posts that proposes a first time.
- `ChallengesPanel` / **Requests** (header button) runs negotiation: Accept /
  Nudge time / Decline; scheduled matches show under **Scheduled**.
- `MatchModal`: no check-in. After the scheduled time the winner posts the
  score within **24 hours**; the loser **confirms or disputes within 48
  hours** or it auto-confirms (client-enforced for now). Messaging and
  cancel-before-play still available on `CONTRACTED` matches.
- "+ Court" mode lets any signed-in user drop a new court on the map
  (`AddCourtPanel` computes its geohash on create).

## Next steps (not yet built)

- Custom `finalizeMatch` mutation (Lambda) to enforce state transitions and
  compute Elo server-side — currently rating writes are client-side and
  trusted. This should also own locking the gauntlet on accept: today the
  client can only lock it when the accepter is the gauntlet owner (auth), so
  a challenger-side accept leaves the gauntlet ACTIVE.
- AppSync subscriptions so requests/scores appear live instead of on reload.
- `finalizeMatch` Lambda for server-side deadlines, Elo, and auto-confirm.
- Swap raster OSM tiles for a proper vector style (MapTiler/Protomaps key).
