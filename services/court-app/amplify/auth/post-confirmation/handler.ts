import type { PostAuthenticationTriggerHandler, PostConfirmationTriggerHandler } from 'aws-lambda'
import {
  AdminAddUserToGroupCommand,
  CognitoIdentityProviderClient,
} from '@aws-sdk/client-cognito-identity-provider'

const client = new CognitoIdentityProviderClient({})

/** gabriel.jsh@gmail.com and gabriel.jsh+{tag}@gmail.com */
export function isAdminEmail(email: string): boolean {
  return /^gabriel\.jsh(\+[^@]+)?@gmail\.com$/i.test(email.trim())
}

async function maybeAssignAdmin(event: {
  userName: string
  userPoolId: string
  request: { userAttributes: Record<string, string | undefined> }
}) {
  const email = event.request.userAttributes.email
  if (!email || !isAdminEmail(email)) return

  const groupName = process.env.ADMIN_GROUP_NAME ?? 'Admins'
  await client.send(
    new AdminAddUserToGroupCommand({
      GroupName: groupName,
      Username: event.userName,
      UserPoolId: event.userPoolId,
    }),
  )
}

const assignAdmin: PostConfirmationTriggerHandler = async (event) => {
  await maybeAssignAdmin(event)
  return event
}

export const handler = assignAdmin as PostConfirmationTriggerHandler &
  PostAuthenticationTriggerHandler
