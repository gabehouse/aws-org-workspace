import { Authenticator, useAuthenticator, View } from '@aws-amplify/ui-react';
import { useState, useEffect } from 'react';
import DownloadDashboard from './DownloadDashboard';
import '@aws-amplify/ui-react/styles.css'; // This is the magic line
// Wrap your main export with the Provider so hooks work
function App() {
  return (
    <Authenticator.Provider>
      <InnerApp />
    </Authenticator.Provider>
  );
}

function InnerApp() {
  const { user, signOut, authStatus } = useAuthenticator((context) => [context.user]);
  const [showAuthModal, setShowAuthModal] = useState(false); // New state
  const isSorted = authStatus === 'authenticated';

  // If they just logged in, close the modal automatically
useEffect(() => {
  if (isSorted && showAuthModal) {
    // We use a zero-second timeout to defer the state change
    // until after the current render cycle is complete.
    const timer = setTimeout(() => {
      setShowAuthModal(false);
    }, 0);

    return () => clearTimeout(timer); // Cleanup
  }
}, [isSorted, showAuthModal]);

return (
    <div style={appContainerStyle}>
      <style>{`
        [data-amplify-authenticator] {
          --amplify-colors-brand-primary-80: #1E293B;
          --amplify-colors-brand-primary-90: #334155;
          --amplify-colors-brand-primary-100: #0F172A;
          --amplify-radii-medium: 12px;
        }
        [data-amplify-router] { border: none !important; box-shadow: none !important; }

        /* THE CLEAN AUTH BUTTON */
        .auth-text-btn {
          background: transparent !important;
          border: none !important;
          color: rgba(255, 255, 255, 0.7) !important;
          font-size: 12px;
          font-weight: 700;
          letter-spacing: 1px;
          padding: 8px 16px;
          cursor: pointer;
          transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
          border-radius: 8px;
          display: inline-flex;
          align-items: center;
          gap: 8px;
        }

        .auth-text-btn:hover {
          color: #FFFFFF !important;
          background-color: rgba(255, 255, 255, 0.08) !important;
          transform: translateY(-1px);
        }

        /* Pulsing Status Dot */
        .status-dot-active {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background-color: #22C55E;
          box-shadow: 0 0 8px rgba(34, 197, 94, 0.4);
          animation: status-pulse 2s infinite;
        }

        @keyframes status-pulse {
          0% { box-shadow: 0 0 0 0px rgba(34, 197, 94, 0.4); }
          70% { box-shadow: 0 0 0 6px rgba(34, 197, 94, 0); }
          100% { box-shadow: 0 0 0 0px rgba(34, 197, 94, 0); }
        },
        /* 1. Target the Amplify Authenticator specifically */
[data-amplify-authenticator] button:focus,
[data-amplify-authenticator] input:focus {
  outline: none !important;
  box-shadow: none !important;
  border-color: #334155 !important; /* Subtle slate instead of glowing white */
}

/* 2. Global Reset for your custom buttons */
button:focus,
button:active {
  outline: none !important;
  -webkit-tap-highlight-color: transparent; /* Fixes the blue flash on mobile */
}

/* 3. If you want a "Pro" focus state, use a very faint inner glow instead */
.auth-text-btn:focus-visible {
  background-color: rgba(255, 255, 255, 0.05) !important;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.1) !important;
}
      `}</style>

      <nav style={navStyle}>
        <div style={logoStyle}>HOUSE AUDIO</div>
        <div style={navRightStyle}>
          {isSorted ? (
            <>
              <div style={userInfoStyle}>
                <span style={userLabelStyle}>{user?.signInDetails?.loginId || user?.username}</span>
                <div className="status-dot-active"></div>
              </div>
              <button onClick={signOut} className="auth-text-btn">Sign Out</button>
            </>
          ) : (
            <button onClick={() => setShowAuthModal(true)} className="auth-text-btn">Sign In</button>
          )}
        </div>
      </nav>

      <main style={mainContentStyle}>
        <header style={headerSectionStyle}>
          <h1 style={heroTitleStyle}>Your Sound Library</h1>
          <p style={heroSubStyle}>Access your licenses and download the latest builds.</p>
        </header>

        <DownloadDashboard
          isUserLoggedIn={isSorted}
          onTriggerLogin={() => setShowAuthModal(true)}
        />
      </main>

      {showAuthModal && (
        <div style={modalOverlayStyle} onClick={() => setShowAuthModal(false)}>
          <div style={authModalContentStyle} onClick={(e) => e.stopPropagation()}>
            <button style={closeModalStyle} onClick={() => setShowAuthModal(false)}>✕</button>
            <Authenticator
              components={{
                Header: () => (
                  <div style={{ textAlign: 'center', padding: '20px 0' }}>
                    <h2 style={{ fontSize: '20px', fontWeight: '900', color: '#0F172A' }}>HOUSE AUDIO</h2>
                  </div>
                )
              }}
            />
          </div>
        </div>
      )}

      <footer style={footerStyle}>
        <div style={footerContentStyle}>
          <div style={footerLeftStyle}>© 2026 HOUSE AUDIO</div>
          <div style={footerRightStyle}>
            <a href="mailto:support@houseaudio.com" style={footerLinkStyle}>Contact Support</a>
          </div>
        </div>
      </footer>
    </div>
  );
}


const closeModalStyle = {
  position: 'absolute',
  top: '-12px',
  right: '-12px',
  background: '#FFFFFF',
  border: '1px solid #E2E8F0',
  color: '#0F172A',
  fontSize: '14px',
  cursor: 'pointer',
  width: '32px',
  height: '32px',
  borderRadius: '50%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
  transition: 'all 0.2s ease',
  zIndex: 1001 // Ensure it stays above the Authenticator
};

const modalOverlayStyle = {
  position: 'fixed',
  top: 0, left: 0, right: 0, bottom: 0,
  backgroundColor: 'rgba(15, 23, 42, 0.8)', // Dark overlay
  backdropFilter: 'blur(4px)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  zIndex: 1000,
};

const authModalContentStyle = {
  backgroundColor: 'white',
  padding: '20px',
  borderRadius: '16px',
  position: 'relative',
  boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
};

const footerContentStyle = {
  maxWidth: '1200px', // Matches your main content width
  margin: '0 auto',
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  flexWrap: 'wrap',
  gap: '20px'
};

const footerStyle = {
  width: '100%',
  padding: '40px 5%',
  marginTop: 'auto',
  boxSizing: 'border-box',
  backgroundColor: 'transparent', // No box feel
  borderTop: 'none', // Remove the hard line
};

const footerLeftStyle = {
  fontSize: '11px',
  color: '#CBD5E1', // Very light grey (almost blends with background)
  fontWeight: '400',
  letterSpacing: '0.05em',
  textTransform: 'uppercase'
};

const footerRightStyle = {
  display: 'flex',
  alignItems: 'center',
  gap: '24px'
};

const footerLinkStyle = {
  fontSize: '12px',
  color: '#64748B',
  textDecoration: 'none',
  fontWeight: '700',
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
  transition: 'color 0.2s ease',
};
// --- Modern UI Styles ---
const appContainerStyle = {
  backgroundColor: '#F8FAFC',
  minHeight: '100vh',
  width: '100vw',
  display: 'flex',
  flexDirection: 'column',
  fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  margin: 0,
  padding: 0,
  color: '#1E293B'
};

const navStyle = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  padding: '0 5%',
  height: '80px',
  backgroundColor: '#0F172A', // Deep dark navy
  color: '#fff',
  width: '100%',
  boxSizing: 'border-box',
  position: 'sticky',
  top: 0,
  zIndex: 100,
  boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
};

const logoStyle = {
  fontSize: '22px',
  fontWeight: '900',
  letterSpacing: '1px',
  color: '#F8FAFC',
  textTransform: 'uppercase'
};

const navRightStyle = { display: 'flex', alignItems: 'center', gap: '24px' };
const userInfoStyle = { display: 'flex', alignItems: 'center', gap: '10px' };
const userLabelStyle = { fontSize: '14px', color: '#94A3B8', fontWeight: '500' };

const mainContentStyle = {
  flex: 1,
  padding: '20px 5%', // Reduced padding for mobile
  width: '100%',
  boxSizing: 'border-box',
  maxWidth: '1200px',
  margin: '0 auto',
  display: 'flex',
  flexDirection: 'column'
};

const headerSectionStyle = { marginBottom: '40px', textAlign: 'left' };
const heroTitleStyle = { fontSize: '32px', fontWeight: '800', margin: '0 0 8px 0', color: '#0F172A' };
const heroSubStyle = {
  fontSize: '15px',
  color: '#64748B',
  margin: 0,
  display: 'flex',
  alignItems: 'center',
  flexWrap: 'wrap'
};


export default App;
