import { defineFunction } from '@aws-amplify/backend'

export const applyMatchRatingsFunction = defineFunction({
  name: 'applyMatchRatings',
  entry: './handler.ts',
  runtime: 20,
  resourceGroupName: 'data',
})
