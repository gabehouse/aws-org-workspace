import { defineBackend } from '@aws-amplify/backend';
import { PolicyStatement } from 'aws-cdk-lib/aws-iam';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import { auth } from './auth/resource';
import { data } from './data/resource';
import { sendMatchLockedEmailFunction } from './functions/send-match-locked-email/resource';
import { applyMatchRatingsFunction } from './functions/apply-match-ratings/resource';

const backend = defineBackend({
  auth,
  data,
  sendMatchLockedEmailFunction,
  applyMatchRatingsFunction,
});

const emailLambda = backend.sendMatchLockedEmailFunction.resources.lambda as lambda.Function;

emailLambda.addToRolePolicy(
  new PolicyStatement({
    actions: ['ses:SendEmail', 'ses:SendRawEmail'],
    resources: ['*'],
  }),
);

emailLambda.addToRolePolicy(
  new PolicyStatement({
    actions: ['cognito-idp:AdminGetUser'],
    resources: [backend.auth.resources.userPool.userPoolArn],
  }),
);

emailLambda.addEnvironment(
  'USER_POOL_ID',
  backend.auth.resources.userPool.userPoolId,
);

// Replace with a verified SES identity for your domain before production.
emailLambda.addEnvironment('SENDER_EMAIL', 'no-reply@grandrivertennis.ca');
