import { SESClient, SendEmailCommand } from '@aws-sdk/client-ses'
import {
  CognitoIdentityProviderClient,
  AdminGetUserCommand,
} from '@aws-sdk/client-cognito-identity-provider'

type NotifyMatchLockedInput = {
  arguments: {
    recipientUserId: string
    accepterHandle: string
    courtName: string
    scheduledAt: string
    calendarUrl: string
    format?: string | null
    stakes?: string | null
  }
}

export const handler = async (event: NotifyMatchLockedInput) => {
  const {
    recipientUserId,
    accepterHandle,
    courtName,
    scheduledAt,
    calendarUrl,
    format,
    stakes,
  } = event.arguments

  const userPoolId = process.env.USER_POOL_ID
  const senderEmail = process.env.SENDER_EMAIL
  if (!userPoolId || !senderEmail) {
    throw new Error('Email configuration missing (USER_POOL_ID or SENDER_EMAIL)')
  }

  const cognito = new CognitoIdentityProviderClient({})
  const user = await cognito.send(
    new AdminGetUserCommand({
      UserPoolId: userPoolId,
      Username: recipientUserId,
    }),
  )
  const email = user.UserAttributes?.find((a) => a.Name === 'email')?.Value
  if (!email) throw new Error('Recipient email not found')

  const when = new Date(scheduledAt).toLocaleString('en-CA', {
    dateStyle: 'full',
    timeStyle: 'short',
  })

  const lines = [
    'Your match request was accepted!',
    '',
    `${accepterHandle} locked in your tennis match at ${courtName}.`,
    '',
    `When: ${when}`,
    stakes ? `Stakes: ${stakes}` : null,
    format ? `Format: ${format}` : null,
    '',
    'Add to your calendar:',
    calendarUrl,
    '',
    'See you on court!',
  ].filter((line): line is string => line !== null)

  const ses = new SESClient({})
  await ses.send(
    new SendEmailCommand({
      Source: senderEmail,
      Destination: { ToAddresses: [email] },
      Message: {
        Subject: { Data: `Match locked in at ${courtName}` },
        Body: { Text: { Data: lines.join('\n') } },
      },
    }),
  )

  return { ok: true }
}
