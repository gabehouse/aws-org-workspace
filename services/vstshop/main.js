// main.js
const { Amplify, Auth } = aws_amplify; // Globals from CDN script

Amplify.configure({
    Auth: {
        userPoolId: 'us-east-2_V9hhjw2Y5',
        userPoolClientId: '2p1utd8g6jkts9qp59n7b5k9le',
        region: 'us-east-2'
    }
});

async function testBackend() {
    try {
        // 1. Sign in (using the user we created earlier)
        const user = await Auth.signIn('testuser@example.com', 'YourStrongPassword123!');
        console.log('Logged in!', user);

        // 2. Get the token
        const session = await Auth.currentSession();
        const token = session.getIdToken().getJwtToken();
        console.log('Token acquired');

        // 3. Call your API Gateway
        const response = await fetch('https://<your-api-id>.execute-api.us-east-2.amazonaws.com/dev/check-vst', {
            headers: {
                'Authorization': token
            }
        });

        const data = await response.json();
        document.getElementById('status').innerText = "Success: " + JSON.stringify(data);
    } catch (error) {
        console.error('Error:', error);
        document.getElementById('status').innerText = "Error: " + error.message;
    }
}

document.getElementById('loginBtn').addEventListener('click', testBackend);
