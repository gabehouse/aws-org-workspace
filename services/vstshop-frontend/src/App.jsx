import { useState, useEffect } from 'react';
import { Amplify } from 'aws-amplify';
import { signIn, fetchAuthSession } from 'aws-amplify/auth';

// --- AMPLIFY CONFIGURATION ---
// We define this outside the component so it only runs once when the file is loaded.
const authConfig = {
  Auth: {
    Cognito: {
      userPoolId: import.meta.env.VITE_USER_POOL_ID,
      userPoolClientId: import.meta.env.VITE_USER_POOL_CLIENT_ID,
      region: import.meta.env.VITE_AWS_REGION,
      loginWith: { email: true }
    }
  }
};

// Only try to configure if the variables actually exist.
if (import.meta.env.VITE_USER_POOL_ID) {
  try {
    Amplify.configure(authConfig);
    console.log("Amplify configured successfully");
  } catch (e) {
    console.error("Amplify configuration failed:", e);
  }
}

function App() {
  const [status, setStatus] = useState('Idle');
  const [envError, setEnvError] = useState(false);

  // Check for environment variables on mount
  useEffect(() => {
    if (!import.meta.env.VITE_USER_POOL_ID) {
      setEnvError(true);
      console.error("MISSING ENV: Ensure VITE_USER_POOL_ID is in your .env file.");
    }
  }, []);

  const testBackend = async () => {
    setStatus('Processing...');
    try {
      // 1. Sign In
      // Note: In a real app, you'd use a form for email/password
      await signIn({
        username: 'testuser@example.com',
        password: 'YourStrongPassword123!'
      });
      console.log('Login Success');

      // 2. Get Token
      const session = await fetchAuthSession();
      const token = session.tokens.idToken.toString();

      // 3. Call API
      const apiId = 'g6jnm33pu7';
      const apiUrl = `https://${apiId}.execute-api.us-east-2.amazonaws.com/dev/check-vst`;

      const response = await fetch(apiUrl, {
        method: 'GET',
        headers: { 'Authorization': token }
      });

      if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);

      const data = await response.json();
      setStatus("API Success: " + JSON.stringify(data));

    } catch (error) {
      console.error('Test Error:', error);
      setStatus("Error: " + (error.message || "Check Console"));
    }
  };

  // --- ERROR STATE UI ---
  if (envError) {
    return (
      <div style={{ padding: '40px', background: '#fff0f0', color: '#d00', border: '2px solid red', borderRadius: '8px', margin: '20px' }}>
        <h2>⚠️ Configuration Error</h2>
        <p>Vite is running, but it cannot find your environment variables.</p>
        <ul style={{ textAlign: 'left', display: 'inline-block' }}>
          <li>Ensure <code>.env</code> exists in <code>services/vstshop-frontend/</code></li>
          <li>Ensure variables start with <code>VITE_</code></li>
          <li>Restart your dev server: <code>npm run dev</code></li>
        </ul>
      </div>
    );
  }

  // --- MAIN APP UI ---
  return (
    <div style={{ padding: '40px', fontFamily: 'sans-serif', textAlign: 'center' }}>
      <header style={{ marginBottom: '40px', borderBottom: '1px solid #eee', paddingBottom: '20px' }}>
        <h1 style={{ color: '#232f3e' }}>VST Shop Test Console</h1>
        <p style={{ color: '#666' }}>Connected to: <code style={{ background: '#f4f4f4' }}>{import.meta.env.VITE_AWS_REGION}</code></p>
      </header>

      <main>
        <div style={{ marginBottom: '30px' }}>
          <button
            onClick={testBackend}
            style={{
              padding: '12px 24px',
              fontSize: '16px',
              backgroundColor: '#ff9900',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              fontWeight: 'bold'
            }}
          >
            Login & Test API
          </button>
        </div>

        <div style={{
          marginTop: '20px',
          padding: '20px',
          backgroundColor: '#f9f9f9',
          borderRadius: '8px',
          minHeight: '100px',
          border: '1px solid #ddd'
        }}>
          <h3 style={{ marginTop: 0 }}>System Status</h3>
          <div style={{
            fontSize: '18px',
            fontFamily: 'monospace',
            color: status.includes('Error') ? 'red' : status.includes('Success') ? 'green' : 'black'
          }}>
            {status}
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;
