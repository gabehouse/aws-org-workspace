import { useState } from 'react'
import { Amplify } from 'aws-amplify'
import { signIn, fetchAuthSession } from 'aws-amplify/auth'

// 1. Configure (This usually goes in main.jsx, but can stay here for testing)
Amplify.configure({
  Auth: {
    Cognito: {
      userPoolId: import.meta.env.VITE_USER_POOL_ID,
      userPoolClientId: import.meta.env.VITE_USER_POOL_CLIENT_ID,
      // You can also add the region if needed
      region: import.meta.env.VITE_AWS_REGION,
      loginWith: { email: true }
    }
  }
});

function App() {
  const [status, setStatus] = useState('Idle');

  const testBackend = async () => {
    setStatus('Processing...');
    try {
      // 2. Sign In (Amplify v6 style)
      await signIn({ username: 'testuser@example.com', password: 'YourStrongPassword123!' });
      console.log('Login Success');

      // 3. Get Token
      const session = await fetchAuthSession();
      const token = session.tokens.idToken.toString();

      // 4. Call API
      const apiId = 'g6jnm33pu7';
      const apiUrl = `https://${apiId}.execute-api.us-east-2.amazonaws.com/dev/check-vst`;

      const response = await fetch(apiUrl, {
        method: 'GET',
        headers: { 'Authorization': token }
      });

      const data = await response.json();
      setStatus("API Success: " + JSON.stringify(data));

    } catch (error) {
      console.error('Error:', error);
      setStatus("Error: " + (error.message || "Check Console"));
    }
  }

  return (
    <div style={{ padding: '20px' }}>
      <h1>VST Shop Test</h1>
      <button onClick={testBackend}>Login & Test API</button>
      <div style={{ marginTop: '20px', fontWeight: 'bold' }}>
        Status: {status}
      </div>
    </div>
  )
}

export default App
