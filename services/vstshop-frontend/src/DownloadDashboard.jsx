import { useState, useEffect } from 'react';
import { fetchAuthSession } from 'aws-amplify/auth'; // Modular v6 import

function DownloadDashboard() {
  const [loading, setLoading] = useState(true); // Start loading to check status
  const [isPurchased, setIsPurchased] = useState(false);
  const [error, setError] = useState('');

  // 1. Check purchase status on mount
  useEffect(() => {
    checkStatus();
  }, []);

  const checkStatus = async () => {
    try {
      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString();

      // We call the same endpoint. If it returns 200/URL, they own it.
      const response = await fetch(`${import.meta.env.VITE_API_URL}/check-vst`, {
        method: 'GET',
        headers: { 'Authorization': token }
      });

      if (response.ok) {
        setIsPurchased(true);
      }
    } catch (err) {
      console.log("Not purchased or error:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleBuy = async () => {
    setLoading(true);
    try {
      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString();

      // Call your (yet to be created) Checkout Lambda
      const response = await fetch(`${import.meta.env.VITE_API_URL}/checkout`, {
        method: 'POST',
        headers: { 'Authorization': token }
      });

      const { checkoutUrl } = await response.json();
      window.location.href = checkoutUrl; // Redirect to Stripe
    } catch (err) {
      setError("Failed to start checkout. " + err);
      setLoading(false);
    }
  };

  const triggerDownload = async () => {
    setLoading(true);
    setError('');

    try {
      // 1. Get the session (v6 pattern)
      // fetchAuthSession handles token refreshing automatically!
      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString();

      if (!token) throw new Error("No active session found. Please log in again.");

      // 2. Call your API Gateway
      const apiUrl = `${import.meta.env.VITE_API_URL}/check-vst`;

      const response = await fetch(apiUrl, {
        method: 'GET',
        headers: {
          'Authorization': token,
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Server error: ${response.status}`);
      }

      const data = await response.json();

      // 3. Trigger S3 Download
      if (data.downloadUrl) {
        console.log("Redirecting to secure S3 link...");
        window.location.href = data.downloadUrl;
      } else {
        throw new Error("Download link not found in response.");
      }

    } catch (err) {
      console.error("Download Error:", err);
      setError(err.message || "Failed to get download link");
    } finally {
      setLoading(false);
    }
  };
if (loading && !isPurchased) return <p>Loading your library...</p>;

  return (
    <div style={cardStyle}>
      <h3 style={{ marginTop: 0 }}>🎵 My VST Library</h3>
      <div style={itemRowStyle}>
        <div style={{ textAlign: 'left' }}>
          <div style={{ fontWeight: 'bold' }}>Cool Synth Plugin</div>
          <div style={{ fontSize: '12px', color: '#666' }}>
            {isPurchased ? "Owned ✅" : "$29.99"}
          </div>
        </div>

        {isPurchased ? (
          <button onClick={triggerDownload} style={downloadBtnStyle}>
            Download
          </button>
        ) : (
          <button onClick={handleBuy} style={buyBtnStyle}>
            Buy Now
          </button>
        )}
      </div>
      {error && <p style={errorStyle}>⚠️ {error}</p>}
    </div>
  );
}

// --- Styles ---
const cardStyle = { margin: '20px auto', maxWidth: '450px', padding: '20px', backgroundColor: '#fff', borderRadius: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)', border: '1px solid #eee' };
const itemRowStyle = { display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#f9f9f9', padding: '15px', borderRadius: '8px', border: '1px solid #ececec' };
const downloadBtnStyle = { backgroundColor: '#ff9900', color: 'white', border: 'none', padding: '10px 20px', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' };
const buyBtnStyle = { backgroundColor: '#007bff', color: 'white', border: 'none', padding: '10px 20px', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' };
const errorStyle = { color: '#d9534f', fontSize: '13px', marginTop: '15px' };

export default DownloadDashboard;
