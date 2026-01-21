import { useEffect, useState } from 'react';
import axios from 'axios';
import { GoogleMap, LoadScript, Marker } from '@react-google-maps/api';

function Servis({ user }) {
  const [info, setInfo] = useState({ contactEmail: '', contactPhone: '', aboutText: '', latitude: 45.815399, longitude: 15.966568 });
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState(info);
  const [msg, setMsg] = useState('');
  const [msgType, setMsgType] = useState('');
  const [saving, setSaving] = useState(false);

  const containerStyle = {
    width: '100%',
    height: '400px'
  };

  useEffect(() => {
    axios.get('/api/servis').then(r => setInfo(r.data)).catch(e => console.error(e));
  }, []);

  useEffect(() => setForm(info), [info]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    
    const newValue = (name === 'latitude' || name === 'longitude') ? parseFloat(value) || '' : value;
    setForm({ ...form, [name]: newValue });
  };

  const handleMapClick = (e) => {
    const lat = e.latLng.lat();
    const lng = e.latLng.lng();
    setForm({ ...form, latitude: lat, longitude: lng });
    setMsg('Lokacija odabrana');
    setMsgType('info');
    setTimeout(() => setMsg(''), 3000);
  };

  const save = async () => {
    setMsg('');
    setMsgType('');
    
    // Validacija
    if (!form.latitude || !form.longitude) {
      setMsg('Trebate odabrati lokaciju na mapi');
      setMsgType('error');
      return;
    }

    setSaving(true);
    try {
      const token = sessionStorage.getItem('auth_token');
      const headers = token ? { Authorization: `Bearer ${token}` } : {};
      const resp = await axios.post('/api/servis', form, { headers });
      setInfo(resp.data);
      setMsg('Spremljeno');
      setMsgType('success');
      setEditing(false);
      
      window.dispatchEvent(new Event('servis-updated'));
    } catch (err) {
      console.error(err);
      const message = err?.response?.data?.message || err?.message || 'Greška pri spremanju';
      setMsg(message);
      setMsgType('error');
    } finally {
      setSaving(false);
    }
  };

  const apiKey = process.env.REACT_APP_GOOGLE_MAPS_API_KEY;

  return (
    <div className="container">
      <h2>Servis</h2>

      <div className="card p-3 mb-3">
        <h5>Kontakt</h5>
        <p>Tel: {info.contactPhone}</p>
        <p>Email: {info.contactEmail}</p>
      </div>

      <div className="card p-3 mb-3">
        <h5>O nama</h5>
        <div dangerouslySetInnerHTML={{ __html: info.aboutText }} />
      </div>

      {user && user.uloga === 'administrator' && (
        <div className="card p-3">
          <h5>Uredi servis (admin)</h5>
          <div className="mb-2">
            <label className="form-label">Email</label>
            <input name="contactEmail" value={form.contactEmail} onChange={handleChange} className="form-control" />
          </div>
          <div className="mb-2">
            <label className="form-label">Telefon</label>
            <input name="contactPhone" value={form.contactPhone} onChange={handleChange} className="form-control" />
          </div>
          <div className="mb-2">
            <label className="form-label">O nama (HTML allowed)</label>
            <textarea name="aboutText" value={form.aboutText} onChange={handleChange} className="form-control" rows={6} />
          </div>

          <div className="mb-3">
            <label className="form-label">Lokacija servisa</label>
            <p className="text-muted small">Kliknite na mapu da odaberete lokaciju</p>
            <div className="mb-2">
              {!apiKey ? (
                <div style={{ width: '100%', height: '400px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  Nedostaje Google Maps API key.
                </div>
              ) : (
                <LoadScript googleMapsApiKey={apiKey}>
                  <GoogleMap
                    mapContainerStyle={containerStyle}
                    center={{ lat: form.latitude || 45.815399, lng: form.longitude || 15.966568 }}
                    zoom={13}
                    onClick={handleMapClick}
                  >
                    {form.latitude && form.longitude && (
                      <Marker position={{ lat: form.latitude, lng: form.longitude }} />
                    )}
                  </GoogleMap>
                </LoadScript>
              )}
            </div>
            <div className="row">
              <div className="col-md-6 mb-2">
                <label className="form-label">Geografska širina (Latitude)</label>
                <input 
                  type="number" 
                  name="latitude" 
                  value={form.latitude || ''} 
                  onChange={handleChange} 
                  className="form-control"
                  step="0.000001"
                  placeholder="45.815399"
                />
              </div>
              <div className="col-md-6 mb-2">
                <label className="form-label">Geografska dužina (Longitude)</label>
                <input 
                  type="number" 
                  name="longitude" 
                  value={form.longitude || ''} 
                  onChange={handleChange} 
                  className="form-control"
                  step="0.000001"
                  placeholder="15.966568"
                />
              </div>
            </div>
          </div>

          <button className="btn btn-primary" onClick={save} disabled={saving}>{saving ? 'Spremanje...' : 'Spremi'}</button>
          <span className={`ms-3 ${msgType === 'success' ? 'text-success' : msgType === 'error' ? 'text-danger' : msgType === 'info' ? 'text-info' : ''}`}>{msg}</span>
        </div>
      )}
    </div>
  );
}

export default Servis;
