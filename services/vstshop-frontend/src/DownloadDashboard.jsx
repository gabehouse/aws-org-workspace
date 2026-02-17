import { useState } from 'react';
import { fetchAuthSession } from 'aws-amplify/auth'; // Modular v6 import

function DownloadDashboard() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

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

  return (
    <div style={{
      margin: '20px auto',
      maxWidth: '450px',
      padding: '20px',
      backgroundColor: '#ffffff',
      borderRadius: '12px',
      boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
      border: '1px solid #eee'
    }}>
      <h3 style={{ marginTop: 0 }}>🎵 My VST Library</h3>
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        background: '#f9f9f9',
        padding: '15px',
        borderRadius: '8px',
        border: '1px solid #ececec'
      }}>
        <div style={{ textAlign: 'left' }}>
          <div style={{ fontWeight: 'bold' }}>Cool Synth Plugin</div>
          <div style={{ fontSize: '12px', color: '#666' }}>Format: .zip (VST3/AU)</div>
        </div>
        <button
          onClick={triggerDownload}
          disabled={loading}
          style={{
            backgroundColor: loading ? '#ccc' : '#ff9900',
            color: 'white',
            border: 'none',
            padding: '10px 20px',
            borderRadius: '6px',
            fontWeight: 'bold',
            transition: '0.2s',
            cursor: loading ? 'not-allowed' : 'pointer'
          }}
        >
          {loading ? 'Verifying...' : 'Download'}
        </button>
      </div>

      {error && (
        <p style={{
          color: '#d9534f',
          fontSize: '13px',
          marginTop: '15px',
          backgroundColor: '#fdf7f7',
          padding: '8px',
          borderRadius: '4px'
        }}>
          ⚠️ {error}
        </p>
      )}
    </div>
  );
}

export default DownloadDashboard;
