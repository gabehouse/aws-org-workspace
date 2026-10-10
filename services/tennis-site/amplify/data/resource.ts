import { type ClientSchema, a, defineData } from '@aws-amplify/backend';
import { bookingFunction } from '../functions/booking/resource';

const slotArguments = {
  dateSlot: a.string().required(), // YYYY-MM-DD
  timeSlot: a.string().required(), // HH:MM
};

const schema = a.schema({
  // Public view of the schedule. A row exists only while the slot is booked and holds no personal info.
  Slot: a
    .model(slotArguments)
    .identifier(['dateSlot', 'timeSlot'])
    .authorization((allow) => [allow.publicApiKey().to(['read']), allow.authenticated().to(['read'])]),

  // Who booked a slot. Readable only by the booker and admins; written only by the booking Lambda.
  Booking: a
    .model({
      ...slotArguments,
      owner: a.string().required(),
      firstName: a.string(),
      lastName: a.string(),
      email: a.string(),
    })
    .identifier(['dateSlot', 'timeSlot'])
    .authorization((allow) => [
      allow.ownerDefinedIn('owner').identityClaim('sub').to(['read']),
      allow.groups(['Admins']).to(['read']),
    ]),

  WaitlistEntry: a
    .model({
      email: a.string().required(),
      firstName: a.string(),
      lastName: a.string(),
      createdAt: a.datetime().required(),
    })
    .authorization((allow) => [allow.owner(), allow.groups(['Admins']).to(['read', 'delete'])]),

  bookSlot: a
    .mutation()
    .arguments(slotArguments)
    .returns(a.boolean())
    .authorization((allow) => [allow.authenticated()])
    .handler(a.handler.function(bookingFunction)),

  cancelBooking: a
    .mutation()
    .arguments(slotArguments)
    .returns(a.boolean())
    .authorization((allow) => [allow.authenticated()])
    .handler(a.handler.function(bookingFunction)),
});

export type Schema = ClientSchema<typeof schema>;

export const data = defineData({
  schema,
  authorizationModes: {
    defaultAuthorizationMode: 'userPool',
    apiKeyAuthorizationMode: {
      expiresInDays: 30,
    },
  },
});
