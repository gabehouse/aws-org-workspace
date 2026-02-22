import { useState, useEffect } from 'react';
import { fetchAuthSession } from 'aws-amplify/auth';
import productConfig from './product_config.json';

function DownloadDashboard() {
  const [loading, setLoading] = useState(true);
  const [ownedProducts, setOwnedProducts] = useState([]);
  const [error, setError] = useState('');
  const [downloadingId, setDownloadingId] = useState(null);

  useEffect(() => { fetchUserLibrary(); }, []);

  const fetchUserLibrary = async () => {
    try {
      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString();
      const response = await fetch(`${import.meta.env.VITE_API_URL}/my-purchases`, {
        headers: { 'Authorization': token }
      });
      if (response.ok) setOwnedProducts(await response.json());
    } catch (err) { setError("Could not load your library. " + err); }
    finally { setLoading(false); }
  };

  const handleBuy = async (productId) => {
    try {
      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString();
      const response = await fetch(`${import.meta.env.VITE_API_URL}/checkout?id=${productId}`, {
        method: 'POST',
        headers: { 'Authorization': token }
      });
      const { checkoutUrl } = await response.json();
      window.location.href = checkoutUrl;
    } catch (err) { setError("Checkout failed. " + err); }
  };

  const triggerDownload = async (productId) => {
    setDownloadingId(productId);
    try {
      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString();
      const response = await fetch(`${import.meta.env.VITE_API_URL}/download?productId=${productId}`, {
        headers: { 'Authorization': token }
      });
      const { downloadUrl } = await response.json();
      window.location.assign(downloadUrl);
    } catch (err) { setError(`Download failed. ` + err); }
    finally { setDownloadingId(null); }
  };

  if (loading) return <div style={loaderStyle}>Checking your library...</div>;

  return (
    <div style={gridStyle}>
      {productConfig.map((product) => {
        const isOwned = ownedProducts.includes(product.id);
        const isProcessing = downloadingId === product.id;

        return (
          <div key={product.id} style={cardStyle}>
            <div style={iconBoxStyle}>🎹</div>
            <h2 style={productNameStyle}>{product.name}</h2>
            <p style={priceStyle}>{isOwned ? "Verified Member ✅" : product.price}</p>

            <div style={actionAreaStyle}>
              {isOwned ? (
                <button
                  onClick={() => triggerDownload(product.id)}
                  style={isProcessing ? disabledBtnStyle : downloadBtnStyle}
                  disabled={isProcessing}
                >
                  {isProcessing ? "Preparing..." : "Download VST"}
                </button>
              ) : (
                <button onClick={() => handleBuy(product.id)} style={buyBtnStyle}>
                  Unlock Now
                </button>
              )}
            </div>
          </div>
        );
      })}
      {error && <p style={errorStyle}>⚠️ {error}</p>}
    </div>
  );
}

// --- Classic Card Styles ---
const gridStyle = { display: 'flex', flexDirection: 'column', gap: '30px', alignItems: 'center', width: '100%' };
const cardStyle = { backgroundColor: '#fff', width: '380px', padding: '40px', borderRadius: '16px', boxShadow: '0 10px 25px rgba(0,0,0,0.05)', textAlign: 'center', border: '1px solid #f0f0f0' };
const iconBoxStyle = { fontSize: '40px', marginBottom: '20px' };
const productNameStyle = { fontSize: '22px', fontWeight: '700', marginBottom: '8px', color: '#111' };
const priceStyle = { fontSize: '15px', color: '#888', marginBottom: '30px' };
const actionAreaStyle = { display: 'flex', justifyContent: 'center' };

const baseBtnStyle = { width: '100%', padding: '14px', borderRadius: '8px', fontWeight: '700', fontSize: '15px', cursor: 'pointer', border: 'none', transition: 'transform 0.1s active' };
const downloadBtnStyle = { ...baseBtnStyle, backgroundColor: '#000', color: '#fff' };
const buyBtnStyle = { ...baseBtnStyle, backgroundColor: '#007bff', color: '#fff' };
const disabledBtnStyle = { ...baseBtnStyle, backgroundColor: '#eee', color: '#aaa', cursor: 'not-allowed' };
const errorStyle = { color: '#d9534f', marginTop: '20px' };
const loaderStyle = { fontSize: '14px', color: '#666' };

export default DownloadDashboard;
