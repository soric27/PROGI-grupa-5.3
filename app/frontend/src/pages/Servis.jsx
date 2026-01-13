import { useEffect, useState } from 'react';
import axios from 'axios';

function Servis({ user }) {
  const [info, setInfo] = useState({ contactEmail: '', contactPhone: '', aboutText: '' });
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState(info);
  const [msg, setMsg] = useState('');

  useEffect(() => {
    axios.get('/api/servis').then(r => setInfo(r.data)).catch(e => console.error(e));
  }, []);

  useEffect(() => setForm(info), [info]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const save = async () => {
    try {
      const resp = await axios.post('/api/servis', form);
      setInfo(resp.data);
      setMsg('Spremljeno');
      setEditing(false);
    } catch (err) {
      console.error(err);
      setMsg(err?.response?.data?.message || 'Greška pri spremanju');
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

          <button className="btn btn-primary" onClick={save}>Spremi</button>
          <span className="text-success ms-3">{msg}</span>
        </div>
      )}
    </div>
  );
}

export default Servis;
