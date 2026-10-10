import type { AppSyncIdentityCognito } from 'aws-lambda';
import { DynamoDBClient } from '@aws-sdk/client-dynamodb';
import { DynamoDBDocumentClient, GetCommand, TransactWriteCommand } from '@aws-sdk/lib-dynamodb';
import { AdminGetUserCommand, CognitoIdentityProviderClient } from '@aws-sdk/client-cognito-identity-provider';
import { isBookable } from '../../shared/schedule';

const ddb = DynamoDBDocumentClient.from(new DynamoDBClient({}));
const cognito = new CognitoIdentityProviderClient({});

const SLOT_TABLE = process.env.SLOT_TABLE!;
const BOOKING_TABLE = process.env.BOOKING_TABLE!;
const USER_POOL_ID = process.env.USER_POOL_ID!;

// Amplify's function resolver sends fieldName at the top level, not under `info` like a plain AppSync Lambda event.
type BookingEvent = {
  fieldName: string;
  arguments: { dateSlot: string; timeSlot: string };
  identity: AppSyncIdentityCognito;
};

export const handler = async (event: BookingEvent): Promise<boolean> => {
  const { identity, fieldName } = event;
  const { dateSlot, timeSlot } = event.arguments;

  switch (fieldName) {
    case 'bookSlot':
      return bookSlot(identity, dateSlot, timeSlot);
    case 'cancelBooking':
      return cancelBooking(identity, dateSlot, timeSlot);
    default:
      throw new Error(`Unsupported field: ${fieldName}`);
  }
};

async function bookSlot(identity: AppSyncIdentityCognito, dateSlot: string, timeSlot: string) {
  if (!isBookable(dateSlot, timeSlot)) {
    throw new Error('That slot is not open for booking.');
  }

  const profile = await getProfile(identity.username);
  const now = new Date().toISOString();
  const key = { dateSlot, timeSlot };

  // Both rows are written in one transaction, and each Put fails if the row already exists,
  // so two people booking the same slot at once can't both succeed.
  try {
    await ddb.send(
      new TransactWriteCommand({
        TransactItems: [
          {
            Put: {
              TableName: SLOT_TABLE,
              Item: { ...key, __typename: 'Slot', createdAt: now, updatedAt: now },
              ConditionExpression: 'attribute_not_exists(dateSlot)',
            },
          },
          {
            Put: {
              TableName: BOOKING_TABLE,
              Item: { ...key, __typename: 'Booking', owner: identity.sub, ...profile, createdAt: now, updatedAt: now },
              ConditionExpression: 'attribute_not_exists(dateSlot)',
            },
          },
        ],
      }),
    );
  } catch (error) {
    if (isConditionFailure(error)) {
      throw new Error('Sorry, that slot was just booked by someone else.');
    }
    throw error;
  }
  return true;
}

async function cancelBooking(identity: AppSyncIdentityCognito, dateSlot: string, timeSlot: string) {
  const key = { dateSlot, timeSlot };
  const { Item: booking } = await ddb.send(new GetCommand({ TableName: BOOKING_TABLE, Key: key }));
  if (!booking) {
    throw new Error('There is no booking for that slot.');
  }

  const isAdmin = identity.groups?.includes('Admins') ?? false;
  if (booking.owner !== identity.sub && !isAdmin) {
    throw new Error('You can only cancel your own booking.');
  }

  try {
    await ddb.send(
      new TransactWriteCommand({
        TransactItems: [
          {
            Delete: {
              TableName: BOOKING_TABLE,
              Key: key,
              // Guards against deleting a different booking made after the read above.
              ConditionExpression: '#owner = :owner',
              ExpressionAttributeNames: { '#owner': 'owner' },
              ExpressionAttributeValues: { ':owner': booking.owner },
            },
          },
          { Delete: { TableName: SLOT_TABLE, Key: key } },
        ],
      }),
    );
  } catch (error) {
    if (isConditionFailure(error)) {
      throw new Error('That booking changed while you were cancelling it. Please refresh and try again.');
    }
    throw error;
  }
  return true;
}

async function getProfile(username: string) {
  const { UserAttributes = [] } = await cognito.send(
    new AdminGetUserCommand({ UserPoolId: USER_POOL_ID, Username: username }),
  );
  const attr = (name: string) => UserAttributes.find((a) => a.Name === name)?.Value ?? null;
  return { firstName: attr('given_name'), lastName: attr('family_name'), email: attr('email') };
}

function isConditionFailure(error: unknown): boolean {
  if (!(error instanceof Error) || error.name !== 'TransactionCanceledException') {
    return false;
  }
  const reasons = (error as Error & { CancellationReasons?: { Code?: string }[] }).CancellationReasons ?? [];
  return reasons.some((r) => r.Code === 'ConditionalCheckFailed');
}
