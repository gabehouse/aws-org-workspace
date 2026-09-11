import { defineFunction } from '@aws-amplify/backend'

export const sendMatchLockedEmailFunction = defineFunction({
  name: 'sendMatchLockedEmail',
  entry: './handler.ts',
  runtime: 20,
  resourceGroupName: 'auth',
})
