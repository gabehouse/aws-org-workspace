import { defineBackend } from '@aws-amplify/backend';
import { Duration, Stack } from 'aws-cdk-lib';
import { Policy, PolicyStatement } from 'aws-cdk-lib/aws-iam';
import { EventSourceMapping, FilterCriteria, FilterRule, StartingPosition } from 'aws-cdk-lib/aws-lambda';
import { SqsDlq } from 'aws-cdk-lib/aws-lambda-event-sources';
import { Queue } from 'aws-cdk-lib/aws-sqs';
import { auth } from './auth/resource';
import { data } from './data/resource';
import { bookingFunction } from './functions/booking/resource';
import { bookingEmailFunction } from './functions/booking-email/resource';

const backend = defineBackend({
  auth,
  data,
  bookingFunction,
  bookingEmailFunction,
});

const slotTable = backend.data.resources.tables['Slot'];
const bookingTable = backend.data.resources.tables['Booking'];
const userPool = backend.auth.resources.userPool;

// Booking Lambda: the only writer of the Slot and Booking tables.
const bookingLambda = backend.bookingFunction.resources.lambda;
slotTable.grantReadWriteData(bookingLambda);
bookingTable.grantReadWriteData(bookingLambda);
userPool.grant(bookingLambda, 'cognito-idp:AdminGetUser');
backend.bookingFunction.addEnvironment('SLOT_TABLE', slotTable.tableName);
backend.bookingFunction.addEnvironment('BOOKING_TABLE', bookingTable.tableName);
backend.bookingFunction.addEnvironment('USER_POOL_ID', userPool.userPoolId);

// Email Lambda: triggered by the Booking table's stream when a booking is created or deleted.
// The stream wiring lives in the Lambda's stack, not the table's nested stack, so references only
// point from the data stack into the table stack and CloudFormation sees no circular dependency.
const emailLambda = backend.bookingEmailFunction.resources.lambda;
const streamStack = Stack.of(emailLambda);

emailLambda.addToRolePolicy(
  new PolicyStatement({
    actions: ['ses:SendEmail'],
    resources: [streamStack.formatArn({ service: 'ses', resource: 'identity', resourceName: '*' })],
  }),
);
backend.bookingEmailFunction.addEnvironment('SENDER_EMAIL', 'no-reply@grandrivertennis.ca');
backend.bookingEmailFunction.addEnvironment('RECIPIENT_EMAIL', 'gabriel.jsh@gmail.com');
backend.bookingEmailFunction.addEnvironment('ENV_NAME', process.env.AWS_BRANCH ?? 'sandbox');

const streamReadPolicy = new Policy(streamStack, 'BookingStreamReadPolicy', {
  statements: [
    new PolicyStatement({
      actions: ['dynamodb:DescribeStream', 'dynamodb:GetRecords', 'dynamodb:GetShardIterator', 'dynamodb:ListStreams'],
      resources: [bookingTable.tableStreamArn!],
    }),
  ],
});
emailLambda.role!.attachInlinePolicy(streamReadPolicy);

const emailDeadLetterQueue = new Queue(streamStack, 'BookingEmailDLQ', {
  retentionPeriod: Duration.days(14),
});

const streamMapping = new EventSourceMapping(streamStack, 'BookingStreamToEmail', {
  target: emailLambda,
  eventSourceArn: bookingTable.tableStreamArn,
  startingPosition: StartingPosition.LATEST,
  batchSize: 10,
  retryAttempts: 3,
  reportBatchItemFailures: true,
  onFailure: new SqsDlq(emailDeadLetterQueue),
  filters: [FilterCriteria.filter({ eventName: FilterRule.or('INSERT', 'REMOVE') })],
});
streamMapping.node.addDependency(streamReadPolicy);
