import { defineAuth } from '@aws-amplify/backend';
import { postConfirmation } from './post-confirmation/resource';

/**
 * Email-only login for the first slice. Social providers (Google, etc.)
 * can be added later via externalProviders, same as tennis-site.
 */
export const auth = defineAuth({
  loginWith: {
    email: true,
  },
  userAttributes: {
    preferredUsername: {
      required: false,
      mutable: true,
    },
  },
  groups: ['Admins'],
  triggers: {
    postConfirmation,
    postAuthentication: postConfirmation,
  },
  access: (allow) => [allow.resource(postConfirmation).to(['addUserToGroup'])],
});
