// main.js
const { Amplify, Auth } = aws_amplify;

Amplify.configure({
    Auth: {
        userPoolId: 'us-east-2_V9hhjw2Y5',
        userPoolClientId: '2p1utd8g6jkts9qp59n7b5k9le',
        region: 'us-east-2'
    }
});

async function testBackend() {
    const statusDiv = document.getElementById('status');
    statusDiv.innerText = "Processing...";

    try {
        // 1. Sign in
        // We use the test user you created earlier
        await Auth.signIn('testuser@example.com', 'YourStrongPassword123!');
        console.log('Login Success');

        // 2. Get the Token
        const session = await Auth.currentSession();
        const token = session.getIdToken().getJwtToken();

        // 3. Call your API Gateway
        // REPLACE 'YOUR_API_ID' with the ID from your Terraform output
        const apiId = 'g6jnm33pu7';
        const apiUrl = `https://${apiId}.execute-api.us-east-2.amazonaws.com/dev/check-vst`;

        const response = await fetch(apiUrl, {
            method: 'GET',
            headers: {
                'Authorization': token
            }
        });

        const data = await response.json();
        statusDiv.innerText = "API Success: " + JSON.stringify(data);

    } catch (error) {
        console.error('Error:', error);
        statusDiv.innerText = "Error: " + (error.message || "Check Console");
    }
}

document.getElementById('loginBtn').addEventListener('click', testBackend);
