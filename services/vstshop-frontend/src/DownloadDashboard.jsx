import { useState, useEffect } from 'react';
import { fetchAuthSession } from 'aws-amplify/auth';
import productConfig from './product_config.json';

// --- SUB-COMPONENT: INDIVIDUAL PRODUCT CARD ---
const ProductCard = ({ product, isOwned, isUserLoggedIn, isProcessing, onBuy, onDownload, onShowVideo }) => {
  const [showChangelog, setShowChangelog] = useState(false);
  const [imgLoaded, setImgLoaded] = useState(false);

  const cloudfrontBase = import.meta.env.VITE_CLOUDFRONT_URL;
  const fullImageUrl = `${cloudfrontBase}/${product.imagePath}`;

  // Local changelog data - in a real app, this would come from the JSON/DB
  const changelog = [
    { version: "v1.0.4", date: "Feb 2026", note: "Improved CPU efficiency by 15%." },
    { version: "v1.0.2", date: "Jan 2026", note: "Added 20 new 'House Essentials' presets." },
    { version: "v1.0.0", date: "Dec 2025", note: "Initial Release." }
  ];

  return (
    <div style={featuredCardStyle}>
      {/* LEFT: GUI IMAGE & SKELETON */}
      <div style={imageSectionStyle}>
        {!imgLoaded && <div style={skeletonStyle} />}
        <img
          src={fullImageUrl}
          alt={`${product.name} Interface`}
          style={{
            ...guiImageStyle,
            opacity: imgLoaded ? 1 : 0,
            transition: 'opacity 0.5s ease, transform 0.5s cubic-bezier(0.4, 0, 0.2, 1)'
          }}
          onLoad={() => setImgLoaded(true)}
          onMouseEnter={(e) => imgLoaded && (e.target.style.transform = 'scale(1.03)')}
          onMouseLeave={(e) => imgLoaded && (e.target.style.transform = 'scale(1)')}
          onError={(e) => { e.target.src = 'https://via.placeholder.com/600x400?text=Image+Not+Found'; }}
        />
        <button
          style={watchDemoBtnStyle}
          onClick={() => onShowVideo(product)}
        >
          <span style={{ fontSize: '18px' }}>▶</span> Watch Demo
        </button>
      </div>

      {/* RIGHT: DETAILS SECTION */}
      <div style={detailsSectionStyle}>
        <div style={topMetaRowStyle}>
          <button
            onClick={() => setShowChangelog(!showChangelog)}
            style={versionToggleBtnStyle}
          >
            {showChangelog ? "✕ Hide Log" : `⚙️ ${product.version || 'v1.0.4'}`}
          </button>

          <div style={badgeStyle(isOwned)}>
            {isOwned ? "LICENSED" : "PREMIUM"}
          </div>
        </div>

        <h2 style={productNameStyle}>{product.name}</h2>
        <p style={descriptionStyle}>
          Character-driven distortion designed to emulate the unstable, asymmetric growl of vintage hardware transistors. Transforms any source into a thick, snarling texture.
        </p>

        <div style={statsRowStyle}>
          <div style={statBox}><strong>Format:</strong> VST3 (64-bit)</div>
          <div style={statBox}><strong>OS:</strong> macOS & Windows</div>
        </div>

        <div style={purchaseRowStyle}>
          <div style={priceInfoStyle}>
            <div style={priceLabelStyle}>{isOwned ? "LICENSE" : "PRICE"}</div>
            <div style={priceValueStyle}>{isOwned ? "Lifetime Active" : product.price}</div>
          </div>

          <div style={buttonWrapperStyle}>
            {isUserLoggedIn && isOwned ? (
              <button
                onClick={() => onDownload(product.id)}
                style={isProcessing ? disabledBtnStyle : downloadBtnStyle}
                disabled={isProcessing}
              >
                {isProcessing ? "Preparing..." : "Download"}
              </button>
            ) : (
              <button onClick={() => onBuy(product.id)} style={buyBtnStyle}>
                Purchase
              </button>
            )}
          </div>
        </div>
      </div>

      {/* EXPANDABLE CHANGELOG */}
      {showChangelog && (
        <div style={inlineChangelogStyle}>
          <div style={changelogHeaderStyle}>Technical Update History</div>
          <div style={changelogGridStyle}>
            {changelog.map((item, index) => (
              <div key={index} style={changelogItemStyle}>
                <strong style={{color: '#0F172A'}}>{item.version}</strong>
                <span style={{color: '#94A3B8', fontSize: '11px'}}>{item.date}</span>
                <span style={{color: '#475569'}}>{item.note}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

// --- MAIN DASHBOARD COMPONENT ---
function DownloadDashboard({ isUserLoggedIn, onTriggerLogin }) {
  const [loading, setLoading] = useState(true);
  const [ownedProducts, setOwnedProducts] = useState([]);
  const [error, setError] = useState('');
  const [downloadingId, setDownloadingId] = useState(null);
  const [activeVideoProduct, setActiveVideoProduct] = useState(null);
  const [showSuccess, setShowSuccess] = useState(false);

  useEffect(() => {
    if (isUserLoggedIn) {
      fetchUserLibrary();
    } else {
      setOwnedProducts([]);
      setLoading(false);
    }

    const params = new URLSearchParams(window.location.search);
    if (window.location.pathname.includes('/success') || params.get('session_id')) {
      setShowSuccess(true);
      const cleanPath = window.location.pathname.replace('/success', '/');
      window.history.replaceState({}, document.title, cleanPath);
    }
  }, [isUserLoggedIn]);

  const fetchUserLibrary = async () => {
    setLoading(true);
    try {
      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString();
      if (!token) return;

      const response = await fetch(`${import.meta.env.VITE_API_URL}/my-purchases`, {
        headers: { 'Authorization': token }
      });

      if (response.ok) {
        const data = await response.json();
        setOwnedProducts(data);
      }
    } catch (err) {
      console.warn("Auth session not available for guest." + err);
    } finally {
      setLoading(false);
    }
  };

  const handleBuy = async (productId) => {
    if (!isUserLoggedIn) {
      onTriggerLogin();
      return;
    }
    try {
      const session = await fetchAuthSession();
      const token = session.tokens?.idToken?.toString();
      const response = await fetch(`${import.meta.env.VITE_API_URL}/checkout?id=${productId}`, {
        method: 'POST',
        headers: { 'Authorization': token }
      });
      const { checkoutUrl } = await response.json();
      window.location.href = checkoutUrl;
    } catch (err) { setError("Checkout failed."+ err); }
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
    } catch (err) { setError(`Download failed.` + err); }
    finally { setDownloadingId(null); }
  };

  if (loading) return <div style={loaderStyle}>Loading House Audio Library...</div>;

  return (
    <div style={containerStyle}>
      {productConfig.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
          isOwned={ownedProducts.includes(product.id)}
          isUserLoggedIn={isUserLoggedIn}
          isProcessing={downloadingId === product.id}
          onBuy={handleBuy}
          onDownload={triggerDownload}
          onShowVideo={(prod) => setActiveVideoProduct(prod)}
        />
      ))}

      {/* VIDEO MODAL (SHARED) */}
      {activeVideoProduct && (
        <div style={modalOverlayStyle} onClick={() => setActiveVideoProduct(null)}>
          <div style={modalContentStyle} onClick={e => e.stopPropagation()}>
            <button style={closeModalStyle} onClick={() => setActiveVideoProduct(null)}>X</button>
            <iframe
              width="100%" height="100%"
              src={`https://www.youtube.com/embed/${activeVideoProduct.youtubeId}?autoplay=1`}
              title="YouTube video player" frameBorder="0" allowFullScreen
            ></iframe>
          </div>
        </div>
      )}

      {/* SUCCESS MODAL */}
      {showSuccess && (
        <div style={modalOverlayStyle} onClick={() => setShowSuccess(false)}>
          <div style={successContentStyle} onClick={e => e.stopPropagation()}>
            <div style={successIconStyle}>✓</div>
            <h2 style={{ margin: '0 0 10px 0', color: '#0F172A', fontSize: '24px' }}>Payment Received!</h2>
            <p style={{ color: '#64748B', lineHeight: '1.5', marginBottom: '25px' }}>
              Your license is active and your VST is ready for download.
            </p>
            <button style={downloadBtnStyle} onClick={() => setShowSuccess(false)}>Close</button>
          </div>
        </div>
      )}

      {/* Error Toast */}
      {error && (
        <div style={errorBannerStyle}>
          <span style={{ marginRight: '8px' }}>⚠️</span> {error}
          <button onClick={() => setError('')} style={closeErrorBtnStyle}>✕</button>
        </div>
      )}
    </div>
  );
}

// --- STYLES & ANIMATIONS ---

if (typeof document !== 'undefined') {
  const style = document.createElement('style');
  style.innerHTML = `
    @keyframes pulse {
      0% { opacity: 0.3; }
      50% { opacity: 0.6; }
      100% { opacity: 0.3; }
    }
    @keyframes popIn {
      from { transform: scale(0.95); opacity: 0; }
      to { transform: scale(1); opacity: 1; }
    }
  `;
  document.head.appendChild(style);
}

const skeletonStyle = {
  width: '80%',
  height: '70%',
  backgroundColor: '#1E293B',
  borderRadius: '12px',
  position: 'absolute',
  animation: 'pulse 1.5s infinite ease-in-out',
};

// ... (Keep all your existing style constants here) ...
const successContentStyle = { backgroundColor: '#fff', padding: '40px', borderRadius: '24px', textAlign: 'center', maxWidth: '400px', width: '90%', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)', animation: 'popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)' };
const successIconStyle = { width: '60px', height: '60px', backgroundColor: '#DCFCE7', color: '#166534', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '30px', margin: '0 auto 20px auto' };
const containerStyle = { width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '30px', padding: '20px', boxSizing: 'border-box' };
const loaderStyle = { textAlign: 'center', padding: '100px', color: '#64748B', fontWeight: '500' };
const featuredCardStyle = { display: 'flex', flexDirection: 'row', backgroundColor: '#fff', width: '100%', maxWidth: '1100px', borderRadius: '24px', overflow: 'hidden', boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.1)', border: '1px solid #E2E8F0', flexWrap: 'wrap' };
const imageSectionStyle = { flex: '1.4', position: 'relative', backgroundColor: '#0F172A', backgroundImage: 'radial-gradient(circle at center, #1E293B 0%, #0F172A 100%)', minHeight: '450px', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px', boxSizing: 'border-box', overflow: 'hidden' };
const guiImageStyle = { maxWidth: '90%', maxHeight: '90%', objectFit: 'contain', border: '1px solid rgba(255, 255, 255, 0.08)', borderRadius: '4px', boxShadow: '0 50px 100px -20px rgba(0,0,0,0.7)', cursor: 'crosshair' };
const watchDemoBtnStyle = { position: 'absolute', bottom: '25px', left: '50%', transform: 'translateX(-50%)', whiteSpace: 'nowrap', display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(255, 255, 255, 0.08)', backdropFilter: 'blur(10px)', WebkitBackdropFilter: 'blur(10px)', color: '#fff', border: 'none', padding: '8px 18px', borderRadius: '30px', cursor: 'pointer', fontWeight: '600', fontSize: '11px', textTransform: 'uppercase', letterSpacing: '1px' };
const detailsSectionStyle = { flex: '1', padding: '30px', display: 'flex', flexDirection: 'column', width: '100%', flexBasis: '350px', boxSizing: 'border-box' };
const topMetaRowStyle = { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', width: '100%' };
const productNameStyle = { fontSize: '32px', fontWeight: '800', margin: '0 0 15px 0', color: '#0F172A', letterSpacing: '-0.5px' };
const descriptionStyle = { fontSize: '15px', color: '#475569', lineHeight: '1.6', marginBottom: '25px' };
const statsRowStyle = { display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '25px' };
const statBox = { fontSize: '12px', color: '#64748B', backgroundColor: '#F1F5F9', padding: '6px 12px', borderRadius: '8px', border: '1px solid #E2E8F0' };
const badgeStyle = (owned) => ({ fontSize: '11px', fontWeight: '800', padding: '5px 12px', borderRadius: '20px', letterSpacing: '1px', backgroundColor: owned ? '#DCFCE7' : '#EFF6FF', color: owned ? '#166534' : '#2563EB', textTransform: 'uppercase' });
const purchaseRowStyle = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: 'auto', paddingTop: '25px', borderTop: '1px solid #F1F5F9', gap: '20px' };
const priceInfoStyle = { display: 'flex', flexDirection: 'column', gap: '4px', minWidth: 'fit-content' };
const buttonWrapperStyle = { flex: '0 0 140px' };
const priceLabelStyle = { fontSize: '11px', color: '#94A3B8', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.5px' };
const priceValueStyle = { fontSize: '24px', fontWeight: '800', color: '#0F172A', lineHeight: '1' };
const baseBtnStyle = { width: '100%', padding: '12px 16px', borderRadius: '8px', fontWeight: '600', fontSize: '13px', cursor: 'pointer', border: 'none', outline: 'none', transition: 'background-color 0.2s ease, opacity 0.2s ease', letterSpacing: '0.3px' };
const downloadBtnStyle = { ...baseBtnStyle, backgroundColor: '#1E293B', color: '#fff' };
const buyBtnStyle = { ...baseBtnStyle, backgroundColor: '#2563EB', color: '#fff' };
const disabledBtnStyle = { ...baseBtnStyle, backgroundColor: '#F1F5F9', color: '#94A3B8', cursor: 'not-allowed' };
const versionToggleBtnStyle = { backgroundColor: 'transparent', border: '1px solid #E2E8F0', color: '#64748B', fontSize: '11px', fontWeight: '700', padding: '5px 10px', borderRadius: '6px', cursor: 'pointer', textTransform: 'uppercase' };
const inlineChangelogStyle = { flexBasis: '100%', backgroundColor: '#F8FAFC', padding: '30px 40px', borderTop: '1px solid #F1F5F9', boxSizing: 'border-box' };
const changelogHeaderStyle = { fontSize: '12px', fontWeight: '800', color: '#94A3B8', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '15px' };
const changelogGridStyle = { display: 'flex', flexDirection: 'column', width: '100%', gap: '8px' };
const changelogItemStyle = { display: 'flex', flexDirection: 'column', gap: '4px', padding: '12px 0', borderBottom: '1px solid #E2E8F0' };
const modalOverlayStyle = { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.92)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 3000, padding: '40px' };
const modalContentStyle = { width: '100%', maxWidth: '900px', aspectRatio: '16/9', backgroundColor: '#000', borderRadius: '16px', position: 'relative', overflow: 'visible', boxShadow: '0 25px 50px -12px rgba(0,0,0,0.5)' };
const closeModalStyle = { position: 'absolute', top: '-35px', right: '0', background: 'none', border: 'none', color: 'rgba(255, 255, 255, 0.5)', fontSize: '20px', cursor: 'pointer', zIndex: 3001 };
const errorBannerStyle = { position: 'fixed', bottom: '30px', left: '50%', transform: 'translateX(-50%)', backgroundColor: '#FFF1F2', color: '#BE123C', padding: '12px 24px', borderRadius: '12px', border: '1px solid #FDA4AF', boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)', display: 'flex', alignItems: 'center', zIndex: 2000, fontSize: '14px', fontWeight: '500' };
const closeErrorBtnStyle = { background: 'none', border: 'none', color: '#BE123C', marginLeft: '15px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' };

export default DownloadDashboard;
