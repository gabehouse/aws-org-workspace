import { defineAuth, secret } from '@aws-amplify/backend';

export const auth = defineAuth({
  loginWith: {
    email: true,
    externalProviders: {
      google: {
        clientId: secret('GOOGLE_CLIENT_ID'),
        clientSecret: secret('GOOGLE_CLIENT_SECRET'),
        scopes: ['profile', 'openid', 'email'],
        attributeMapping: {
          email: 'email',
          givenName: 'given_name',
          familyName: 'family_name',
        },
      },

      callbackUrls: [
        'http://localhost:5173/',
        'https://main.d34rismepnlcfp.amplifyapp.com/',
        'https://prod.d204uk8pvk2nwc.amplifyapp.com/',
        'https://www.grandrivertennis.ca/',
        'https://grandrivertennis.ca/'
      ],
      logoutUrls: [
        'http://localhost:5173/',
        'https://main.d34rismepnlcfp.amplifyapp.com/',
        'https://prod.d204uk8pvk2nwc.amplifyapp.com/',
        'https://www.grandrivertennis.ca/',
        'https://grandrivertennis.ca/'
      ],
    }
  },
});
