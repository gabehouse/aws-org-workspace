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

  const [downloadUrl, setDownloadUrl] = useState('');

  const checkStatus = async () => {
    const productId = "cool-synth-v1";
    try {
      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString();
      if (!token) throw new Error("No ID Token found");

      // We call the same endpoint. If it returns 200/URL, they own it.
      const response = await fetch(`${import.meta.env.VITE_API_URL}/download?productId=${productId}`, {
              method: 'GET',
              headers: { 'Authorization': token }
          });
      if (response.status === 403) {
        console.error("Still getting a 403. This is now a backend config issue.");
      }
      if (response.ok) {
        const data = await response.json();
        setIsPurchased(true);
        setDownloadUrl(data.downloadUrl);
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

const triggerDownload = async (productId) => {
  // 1. Check if we already have a fresh URL in state from checkStatus
  if (downloadUrl) {
    console.log("Using pre-fetched secure link...");
    window.location.assign(downloadUrl);
    return;
  }

  // 2. FALLBACK: If downloadUrl is empty, fetch it now (Double-safety)
  setLoading(true);
  setError('');
  try {
    const session = await fetchAuthSession();
    const token = session.tokens?.idToken?.toString();
    const apiUrl = `${import.meta.env.VITE_API_URL}/download?productId=${productId}`;

    const response = await fetch(apiUrl, {
      method: 'GET',
      headers: { 'Authorization': token }
    });

    if (!response.ok) throw new Error("Could not verify purchase.");

    const data = await response.json();
    if (data.downloadUrl) {
      setDownloadUrl(data.downloadUrl);
      window.location.assign(data.downloadUrl);
    }
  } catch (err) {
    setError(err.message);
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
          <button onClick={() => triggerDownload("cool-synth-v1")} style={downloadBtnStyle}>
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
