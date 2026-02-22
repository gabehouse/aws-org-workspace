import { Amplify } from 'aws-amplify';
import { Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css';
import DownloadDashboard from './DownloadDashboard';

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
    <div style={appContainerStyle}>
      <Authenticator>
        {({ signOut, user }) => (
          <>
            {/* TOP NAVIGATION BAR */}
            <nav style={navStyle}>
              <div style={logoStyle}>VST Shop</div>
              <div style={navRightStyle}>
                <span style={userLabelStyle}>
                  {user.signInDetails?.loginId || user.username}
                </span>
                <button onClick={signOut} style={signOutBtnStyle}>Sign Out</button>
              </div>
            </nav>

            <main style={mainContentStyle}>
              <DownloadDashboard />
            </main>
          </>
        )}
      </Authenticator>
    </div>
  );
}

// --- App Styles ---
// --- Updated App Styles ---
const appContainerStyle = {
  backgroundColor: '#f4f7f6',
  minHeight: '100vh',
  width: '100vw',           // Force full viewport width
  display: 'flex',          // Ensure we can control the children
  flexDirection: 'column',  // Stack nav and main vertically
  fontFamily: '"Inter", sans-serif',
  margin: 0,
  padding: 0
};

const navStyle = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  padding: '0 40px',
  height: '70px',
  backgroundColor: '#fff',
  borderBottom: '1px solid #e0e0e0',
  width: '100%',            // Ensure nav stretches across
  boxSizing: 'border-box'   // Prevents padding from pushing it past 100%
};

const mainContentStyle = {
  flex: 1,                  // Take up remaining vertical space
  padding: '60px 20px',
  display: 'flex',
  flexDirection: 'column',  // Stack cards vertically
  alignItems: 'center',     // Center the cards horizontally
  width: '100%',            // Ensure main stretches across
  boxSizing: 'border-box'
};
const logoStyle = { fontSize: '20px', fontWeight: '800', letterSpacing: '-0.5px', color: '#1a1a1a' };
const navRightStyle = { display: 'flex', alignItems: 'center', gap: '20px' };
const userLabelStyle = { fontSize: '14px', color: '#666' };
const signOutBtnStyle = { backgroundColor: 'transparent', color: '#d9534f', border: '1px solid #d9534f', padding: '6px 14px', borderRadius: '6px', fontSize: '13px', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s' };

export default App;
