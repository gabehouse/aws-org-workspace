import { defineFunction } from '@aws-amplify/backend';

export const bookingEmailFunction = defineFunction({
  name: 'bookingEmailFunction',
  entry: './handler.ts',
  runtime: 20,
  resourceGroupName: 'data',
});
