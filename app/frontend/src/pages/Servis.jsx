import { useEffect, useState } from 'react';
import axios from 'axios';

function Servis({ user }) {
  const [info, setInfo] = useState({ contactEmail: '', contactPhone: '', aboutText: '' });
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState(info);
  const [msg, setMsg] = useState('');
  const [msgType, setMsgType] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    axios.get('/api/servis').then(r => setInfo(r.data)).catch(e => console.error(e));
  }, []);

  useEffect(() => setForm(info), [info]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const save = async () => {
    setMsg('');
    setMsgType('');
    setSaving(true);
    try {
      const token = sessionStorage.getItem('auth_token');
      const headers = token ? { Authorization: `Bearer ${token}` } : {};
      const resp = await axios.post('/api/servis', form, { headers });
      setInfo(resp.data);
      setMsg('Spremljeno');
      setMsgType('success');
      setEditing(false);
      // notify other pages to refetch servis info
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

          <button className="btn btn-primary" onClick={save} disabled={saving}>{saving ? 'Spremanje...' : 'Spremi'}</button>
          <span className={`ms-3 ${msgType === 'success' ? 'text-success' : msgType === 'error' ? 'text-danger' : ''}`}>{msg}</span>
        </div>
      )}
    </div>
  );
}

export default Servis;
