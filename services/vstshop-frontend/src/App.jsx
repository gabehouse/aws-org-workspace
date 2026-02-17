import { Amplify } from 'aws-amplify';
import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css'; // Essential for styling the forms
import DownloadDashboard from './DownloadDashboard';

// --- AMPLIFY CONFIGURATION ---
Amplify.configure({
  Auth: {
    Cognito: {
      userPoolId: import.meta.env.VITE_USER_POOL_ID,
      userPoolClientId: import.meta.env.VITE_USER_POOL_CLIENT_ID,
      region: import.meta.env.VITE_AWS_REGION,
    }
  }
});

function App() {
  return (
    <div style={{ fontFamily: 'sans-serif', textAlign: 'center', padding: '20px' }}>
      {/* The Authenticator component manages its own loading and user state.
          It will show the Login/Signup forms automatically if no user is found.
      */}
      <Authenticator>
        {({ signOut, user }) => (
          <main style={{ marginTop: '20px' }}>
            <header style={{ background: '#232f3e', color: 'white', padding: '10px', borderRadius: '8px' }}>
              <h1>VST Shop 🔒 Secure Area</h1>
            </header>

            <div style={{ padding: '40px' }}>
              <p>Welcome, <strong>{user.signInDetails?.loginId || user.username}</strong></p>

              {/* Your existing Dashboard logic */}
              <DownloadDashboard />

              <button
                onClick={signOut}
                style={{
                  marginTop: '20px',
                  color: 'red',
                  cursor: 'pointer',
                  background: 'none',
                  border: '1px solid red',
                  padding: '5px 10px',
                  borderRadius: '4px'
                }}
              >
                Sign Out
              </button>
            </div>
          </main>
        )}
      </Authenticator>
    </div>
  );
}

export default App;
