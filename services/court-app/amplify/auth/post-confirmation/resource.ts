import { defineFunction } from '@aws-amplify/backend'

export const postConfirmation = defineFunction({
  name: 'postConfirmation',
  entry: './handler.ts',
  runtime: 20,
  resourceGroupName: 'auth',
  environment: {
    ADMIN_GROUP_NAME: 'Admins',
  },
})
