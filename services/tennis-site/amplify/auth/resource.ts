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
        'http://localhost:5174/',
        'https://www.grandrivertennis.ca/',
        'https://grandrivertennis.ca/',
        'https://feature-as.dkskd07qtjixa.amplifyapp.com/',
        'https://master.dkskd07qtjixa.amplifyapp.com/'
      ],
      logoutUrls: [
        'http://localhost:5174/',
        'https://www.grandrivertennis.ca/',
        'https://grandrivertennis.ca/',
        'https://feature-as.dkskd07qtjixa.amplifyapp.com/',
        'https://master.dkskd07qtjixa.amplifyapp.com/'
      ],
    }
  },
});
