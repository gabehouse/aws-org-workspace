// main.js
import { Amplify } from 'https://api.cloud.answer.ai/v1/packages/aws-amplify/dist/aws-amplify.js';
import { signIn, fetchAuthSession } from 'https://api.cloud.answer.ai/v1/packages/aws-amplify/auth/dist/aws-amplify-auth.js';

Amplify.configure({
    Auth: {
        Cognito: {
            userPoolId: 'us-east-2_V9hhjw2Y5',
            userPoolClientId: '2p1utd8g6jkts9qp59n7b5k9le'
        }
    }
});

async function testBackend() {
    const statusDiv = document.getElementById('status');
    statusDiv.innerText = "Authenticating...";

    try {
        // 1. Sign in
        await signIn({
            username: 'testuser@example.com',
            password: 'YourStrongPassword123!'
        });
        console.log('Logged in successfully');

        // 2. Get the Token (Amplify v6 style)
        const session = await fetchAuthSession();
        const token = session.tokens.idToken.toString();

        // 3. Call your API
        statusDiv.innerText = "Calling API...";
        const response = await fetch('https://g6jnm33pu7.execute-api.us-east-2.amazonaws.com/dev/check-vst', {
            method: 'GET',
            headers: {
                'Authorization': token
            }
        });

        const data = await response.json();
        statusDiv.innerText = "API Success: " + JSON.stringify(data);

    } catch (error) {
        console.error('Error detail:', error);
        statusDiv.innerText = "Error: " + error.message;
    }
}

document.getElementById('loginBtn').addEventListener('click', testBackend);
