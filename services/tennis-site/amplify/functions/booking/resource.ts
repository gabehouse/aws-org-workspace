import { defineFunction } from '@aws-amplify/backend';

export const bookingFunction = defineFunction({
  name: 'bookingFunction',
  entry: './handler.ts',
  runtime: 20,
  // Lives in the data stack because it is both an AppSync resolver and a reader of the data tables.
  resourceGroupName: 'data',
});
