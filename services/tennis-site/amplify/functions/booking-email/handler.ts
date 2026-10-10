import type { DynamoDBBatchResponse, DynamoDBRecord, DynamoDBStreamHandler } from 'aws-lambda';
import type { AttributeValue } from '@aws-sdk/client-dynamodb';
import { unmarshall } from '@aws-sdk/util-dynamodb';
import { SESClient, SendEmailCommand } from '@aws-sdk/client-ses';

const ses = new SESClient({});

const SENDER_EMAIL = process.env.SENDER_EMAIL!;
const RECIPIENT_EMAIL = process.env.RECIPIENT_EMAIL!;
const ENV_NAME = process.env.ENV_NAME ?? 'unknown';

// Triggered by the Booking table's stream (INSERT and REMOVE only). Records are processed in
// order; on the first failure the rest of the batch is handed back to Lambda for retry.
export const handler: DynamoDBStreamHandler = async (event): Promise<DynamoDBBatchResponse> => {
  for (const record of event.Records) {
    try {
      await sendEmailFor(record);
    } catch (error) {
      console.error('Failed to send booking email', { sequenceNumber: record.dynamodb?.SequenceNumber, error });
      return { batchItemFailures: [{ itemIdentifier: record.dynamodb!.SequenceNumber! }] };
    }
  }
  return { batchItemFailures: [] };
};

async function sendEmailFor(record: DynamoDBRecord) {
  const isNew = record.eventName === 'INSERT';
  const image = isNew ? record.dynamodb?.NewImage : record.dynamodb?.OldImage;
  if (!image) {
    return;
  }

  const booking = unmarshall(image as Record<string, AttributeValue>);
  const action = isNew ? 'BOOKING' : 'CANCELLATION';
  const name = [booking.firstName, booking.lastName].filter(Boolean).join(' ') || 'Unknown';

  await ses.send(
    new SendEmailCommand({
      Source: SENDER_EMAIL,
      Destination: { ToAddresses: [RECIPIENT_EMAIL] },
      Message: {
        Subject: { Data: `GRT ${action} ${booking.dateSlot} ${booking.timeSlot} (${ENV_NAME})` },
        Body: {
          Text: {
            Data: `${action}\nDate: ${booking.dateSlot}\nTime: ${booking.timeSlot}\nName: ${name}\nEmail: ${booking.email ?? 'N/A'}`,
          },
        },
      },
    }),
  );
}
