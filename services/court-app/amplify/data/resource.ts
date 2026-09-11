import { type ClientSchema, a, defineData } from '@aws-amplify/backend';
import { applyMatchRatingsFunction } from '../functions/apply-match-ratings/resource';
import { sendMatchLockedEmailFunction } from '../functions/send-match-locked-email/resource';

/**
 * Data model for the gauntlet/match app.
 *
 * Lifecycle (from the whiteboard):
 *   Gauntlet:  ACTIVE -> LOCKED (contract formed) -> ARCHIVED | EXPIRED | WITHDRAWN
 *   Challenge: PENDING -> COUNTERED* -> ACCEPTED | DECLINED | WITHDRAWN | EXPIRED
 *   Match:     CONTRACTED -> LIVE (both checked in) -> REPORTED -> VERIFIED -> FINAL
 *                        \-> RAINED_OUT | CANCELLED          \-> DISPUTED -> FINAL | VOIDED
 *
 * Authorization notes:
 * - Two-party records (Challenge, Match, ScoreReport, Dispute) use
 *   `ownersDefinedIn('participants')`: both players' user ids go in the
 *   participants array at creation time, so either side can update.
 * - Rating fields are client-written for now. Before real users show up,
 *   move Elo updates and match state transitions behind a custom mutation
 *   (Lambda) so players can't write their own ratings.
 */
const schema = a.schema({
  // ---------------------------------------------------------------- players
  PlayerProfile: a
    .model({
      userId: a.string().required(), // Cognito sub; also the owner key
      handle: a.string().required(),
      // Auto-generated at first sign-in. Null until the user's one free
      // rename; after that, renames are allowed every 90 days (client-
      // enforced for now, like other transitions).
      handleChangedAt: a.datetime(),
      displayName: a.string(),
      bio: a.string(),
      homeCourtId: a.id(),
      homeRegionLabel: a.string(),
      homeLat: a.float(),
      homeLng: a.float(),
      // JSON [south, north, west, east] — region zoom scope
      homeBbox: a.string(),
      globalElo: a.integer().default(1200),
      territorialRating: a.integer().default(1200),
      wins: a.integer().default(0),
      losses: a.integer().default(0),
      // "honesty / reliability" judgment from Screen 2
      honorScore: a.integer().default(100),
      matchesCompleted: a.integer().default(0),
      matchesAbandoned: a.integer().default(0),
    })
    .secondaryIndexes((index) => [index('userId'), index('handle'), index('homeRegionLabel')])
    .authorization((allow) => [
      allow.ownerDefinedIn('userId'),
      allow.authenticated().to(['read']),
      allow.guest().to(['read']),
      allow.groups(['Admins']),
    ]),

  // ----------------------------------------------------------------- courts
  CourtSurface: a.enum(['HARD', 'CLAY', 'GRASS', 'CARPET', 'OTHER']),

  Court: a
    .model({
      name: a.string().required(),
      lat: a.float().required(),
      lng: a.float().required(),
      // 5-char geohash cell; the map queries the cells covering the
      // viewport instead of doing a true spatial query
      geohash: a.string().required(),
      address: a.string(),
      surface: a.ref('CourtSurface'),
      courtCount: a.integer().default(1),
      hasLights: a.boolean().default(false),
      isPublic: a.boolean().default(true),
      notes: a.string(),
      gauntlets: a.hasMany('Gauntlet', 'courtId'),
    })
    .secondaryIndexes((index) => [index('geohash')])
    .authorization((allow) => [
      allow.authenticated().to(['read', 'create']),
      allow.guest().to(['read']),
      allow.groups(['Admins']),
    ]),

  // -------------------------------------------------------------- gauntlets
  GauntletStatus: a.enum([
    'ACTIVE', // on the map, accepting challenges
    'LOCKED', // a challenge was accepted; contract formed
    'EXPIRED', // availability window passed
    'WITHDRAWN', // owner pulled it
    'ARCHIVED', // completed its match (or reactivatable, per Screen 1B)
  ]),

  Gauntlet: a
    .model({
      ownerUserId: a.string().required(),
      courtId: a.id().required(),
      court: a.belongsTo('Court', 'courtId'),
      status: a.ref('GauntletStatus').required(),
      // No availability window: a gauntlet is an open "I'm accepting matches"
      // flag. Scheduling happens in the Challenge time-nudging loop.
      // Protocol v1.2 defaults, overridable during negotiation
      stakes: a.string().default(
        'Both players bring 1 unopened ITF-approved can. Play with one; winner takes home the fresh one.',
      ),
      format: a.string().default(
        'ATP best of 3 sets, standard deuce scoring, 7-point tiebreak at 6-6',
      ),
      note: a.string(),
      challenges: a.hasMany('Challenge', 'gauntletId'),
    })
    .secondaryIndexes((index) => [
      // map pins: all ACTIVE gauntlets
      index('status'),
      index('courtId').sortKeys(['status']),
      index('ownerUserId'),
    ])
    .authorization((allow) => [
      allow.ownerDefinedIn('ownerUserId'),
      allow.authenticated().to(['read']),
      allow.guest().to(['read']),
      allow.groups(['Admins']),
    ]),

  // ------------------------------------------------------------- challenges
  ChallengeStatus: a.enum([
    'PENDING',
    'COUNTERED',
    'ACCEPTED',
    'DECLINED',
    'WITHDRAWN',
    'EXPIRED',
  ]),

  Challenge: a
    .model({
      gauntletId: a.id().required(),
      gauntlet: a.belongsTo('Gauntlet', 'gauntletId'),
      challengerUserId: a.string().required(),
      defenderUserId: a.string().required(),
      participants: a.string().required().array().required(),
      status: a.ref('ChallengeStatus').required(),
      // The time currently on the table. Whoever did NOT propose it moves
      // next: accept it, nudge it to a new time, or decline. Nudging swaps
      // proposedByUserId, so turns alternate naturally.
      proposedStart: a.datetime().required(),
      proposedByUserId: a.string().required(),
      message: a.string(),
    })
    .secondaryIndexes((index) => [
      index('gauntletId').sortKeys(['status']),
      index('challengerUserId'),
      index('defenderUserId'),
    ])
    .authorization((allow) => [
      allow.ownersDefinedIn('participants'),
      allow.authenticated().to(['read']),
      allow.guest().to(['read']),
      allow.groups(['Admins']),
    ]),

  // ---------------------------------------------------------------- matches
  MatchStatus: a.enum([
    'CONTRACTED', // Screen 4: terms locked
    'LIVE', // Screen 5 handshake done: both checked in on-site
    'REPORTED', // Screen 6B: one side submitted a score
    'VERIFIED', // Screen 7: both reports agree
    'DISPUTED', // Screen 8: reports conflict
    'FINAL', // ratings applied, loot transferred
    'VOIDED', // dispute unresolvable
    'RAINED_OUT',
    'CANCELLED',
  ]),

  Match: a
    .model({
      gauntletId: a.id().required(),
      challengeId: a.id().required(),
      courtId: a.id().required(),
      defenderUserId: a.string().required(),
      challengerUserId: a.string().required(),
      participants: a.string().required().array().required(),
      status: a.ref('MatchStatus').required(),
      scheduledAt: a.datetime().required(),
      /** When both players locked in (match created). Used for cancellation grace. */
      contractedAt: a.datetime(),
      cancelledAt: a.datetime(),
      stakes: a.string(),
      format: a.string(),
      defenderCheckedInAt: a.datetime(),
      challengerCheckedInAt: a.datetime(),
      startedAt: a.datetime(),
      completedAt: a.datetime(),
      // When the winner posted their score; loser has 48h to confirm/dispute
      scoreReportedAt: a.datetime(),
      // set once VERIFIED; e.g. [{ defender: 4, challenger: 2 }, ...]
      finalSetScores: a.json(),
      winnerUserId: a.string(),
      scoreReports: a.hasMany('ScoreReport', 'matchId'),
      dispute: a.hasOne('Dispute', 'matchId'),
      messages: a.hasMany('MatchMessage', 'matchId'),
    })
    .secondaryIndexes((index) => [
      index('defenderUserId').sortKeys(['scheduledAt']),
      index('challengerUserId').sortKeys(['scheduledAt']),
      index('gauntletId'),
      index('challengeId'),
      index('courtId').sortKeys(['scheduledAt']),
    ])
    .authorization((allow) => [
      allow.ownersDefinedIn('participants'),
      allow.authenticated().to(['read']),
      allow.guest().to(['read']),
      allow.groups(['Admins']),
    ]),

  // ---------------------------------------------------------- match comms
  MatchMessage: a
    .model({
      matchId: a.id().required(),
      match: a.belongsTo('Match', 'matchId'),
      senderUserId: a.string().required(),
      participants: a.string().required().array().required(),
      body: a.string().required(),
      sentAt: a.datetime().required(),
    })
    .secondaryIndexes((index) => [index('matchId').sortKeys(['sentAt'])])
    .authorization((allow) => [
      allow.ownersDefinedIn('participants'),
      allow.authenticated().to(['read']),
      allow.groups(['Admins']),
    ]),

  // ---------------------------------------------------- scoring & disputes
  ScoreReport: a
    .model({
      matchId: a.id().required(),
      match: a.belongsTo('Match', 'matchId'),
      reporterUserId: a.string().required(),
      participants: a.string().required().array().required(),
      // same shape as Match.finalSetScores
      setScores: a.json().required(),
      claimedWinnerUserId: a.string().required(),
    })
    .secondaryIndexes((index) => [index('matchId')])
    .authorization((allow) => [
      allow.ownersDefinedIn('participants'),
      allow.groups(['Admins']),
    ]),

  DisputeStatus: a.enum([
    'OPEN',
    'AWAITING_ADMIN', // winner stood by their score; needs admin resolution
    'RESOLVED_UPHELD', // original report stands
    'RESOLVED_OVERTURNED',
    'MATCH_VOIDED',
  ]),

  Dispute: a
    .model({
      matchId: a.id().required(),
      match: a.belongsTo('Match', 'matchId'),
      raisedByUserId: a.string().required(),
      participants: a.string().required().array().required(),
      status: a.ref('DisputeStatus').required(),
      reason: a.string().required(),
      resolutionNote: a.string(),
      resolvedByUserId: a.string(),
    })
    .secondaryIndexes((index) => [index('matchId')])
    .authorization((allow) => [
      allow.ownersDefinedIn('participants'),
      allow.groups(['Admins']),
    ]),

  // ---------------------------------------------------------------- ratings
  RatingType: a.enum(['GLOBAL_ELO', 'TERRITORIAL']),

  RatingEvent: a
    .model({
      userId: a.string().required(),
      matchId: a.id().required(),
      ratingType: a.ref('RatingType').required(),
      delta: a.integer().required(),
      ratingAfter: a.integer().required(),
      occurredAt: a.datetime().required(),
    })
    .secondaryIndexes((index) => [
      index('userId').sortKeys(['occurredAt']),
      index('matchId'),
    ])
    .authorization((allow) => [
      allow.ownerDefinedIn('userId'),
      allow.authenticated().to(['read']),
      allow.groups(['Admins']),
    ]),

  applyMatchRatings: a
    .mutation()
    .arguments({ matchId: a.id().required() })
    .returns(a.json())
    .authorization((allow) => [allow.authenticated()])
    .handler(a.handler.function(applyMatchRatingsFunction)),

  notifyMatchLocked: a
    .mutation()
    .arguments({
      recipientUserId: a.string().required(),
      accepterHandle: a.string().required(),
      courtName: a.string().required(),
      scheduledAt: a.string().required(),
      calendarUrl: a.string().required(),
      format: a.string(),
      stakes: a.string(),
    })
    .returns(a.json())
    .authorization((allow) => [allow.authenticated()])
    .handler(a.handler.function(sendMatchLockedEmailFunction)),
}).authorization((allow) => [allow.resource(applyMatchRatingsFunction)]);

export type Schema = ClientSchema<typeof schema>;

export const data = defineData({
  schema,
  authorizationModes: {
    defaultAuthorizationMode: 'userPool',
  },
});
