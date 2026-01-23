import { useEffect, useState } from "react";
import axios from "axios";
import "bootstrap/dist/css/bootstrap.min.css";

function Osobe({ user }) {
  const [users, setUsers] = useState([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const [novo, setNovo] = useState({ ime: "", prezime: "", email: "", uloga: "korisnik" });
  const [editing, setEditing] = useState(null); // { id, ime, prezime, email, uloga }

  const fetchUsers = async () => {
    setError("");
    try {
      const r = await axios.get('/api/users');
      setUsers(r.data || []);
    } catch (e) {
      console.error(e);
      setError('Greška pri dohvatu korisnika.');
    }
  };

  useEffect(() => {
    if (user && user.uloga === 'administrator') fetchUsers();
  }, [user]);

  const handleCreate = async (e) => {
    e.preventDefault();
    setLoading(true); setError("");
    try {
      await axios.post('/api/users', novo);
      setNovo({ ime: "", prezime: "", email: "", uloga: "korisnik" });
      await fetchUsers();
    } catch (e) {
      console.error(e);
      setError(e?.response?.data || 'Greška pri dodavanju korisnika.');
    } finally { setLoading(false); }
  };

  const openEdit = (u) => {
    setEditing({ id: u.idOsoba, ime: (u.imePrezime || '').split(" ")[0] || '', prezime: (u.imePrezime || '').split(" ").slice(1).join(" ") || '', email: u.email || '', uloga: u.uloga || 'korisnik' });
    setError("");
  };

  const handleSaveEdit = async () => {
    setLoading(true); setError("");
    try {
      await axios.patch(`/api/users/${editing.id}`, { ime: editing.ime, prezime: editing.prezime, email: editing.email, uloga: editing.uloga });
      setEditing(null);
      await fetchUsers();
    } catch (e) {
      console.error(e);
      setError(e?.response?.data || 'Greška pri ažuriranju korisnika.');
    } finally { setLoading(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Obrisati ovog korisnika?')) return;
    setLoading(true); setError("");
    try {
      await axios.delete(`/api/users/${id}`);
      await fetchUsers();
    } catch (e) {
      console.error(e);
      setError('Greška pri brisanju korisnika.');
    } finally { setLoading(false); }
  };

  const handleSelfRoleChange = async (uloga) => {
    if (!user) return;
    if (!window.confirm(`Promijeniti vlastitu ulogu u "${uloga}"?`)) return;
    setLoading(true); setError(""); setMessage("");
    try {
      const r = await axios.post('/api/auth/select-role', { uloga });
      const token = r?.data?.token;
      if (token) {
        sessionStorage.setItem("auth_token", token);
        axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
      }
      setMessage(`Uloga je promijenjena u "${uloga}".`);
      await fetchUsers();
      window.location.assign("/");
    } catch (e) {
      console.error(e);
      setError(e?.response?.data || 'Greška pri ažuriranju uloge.');
    } finally { setLoading(false); }
  };

  const korisnici = users.filter(u => u.uloga === 'korisnik');
  const serviseri = users.filter(u => u.uloga === 'serviser');

  if (!user || user.uloga !== 'administrator') {
    return <div className="container mt-4"><h4>Nemate pristup ovoj stranici.</h4></div>;
  }

  return (
    <div className="container">
      <h2>Osobe (Administracija)</h2>
      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card mb-4">
        <div className="card-body">
          <h5 className="card-title">Promijeni moju ulogu</h5>
          <p className="text-muted">Ovo mijenja ulogu vašeg računa. Nakon promjene se ponovno prijavite.</p>
          <div className="d-flex gap-2 flex-wrap">
            <button className="btn btn-outline-primary" onClick={() => handleSelfRoleChange("korisnik")} disabled={loading}>Postavi na korisnika</button>
            <button className="btn btn-outline-primary" onClick={() => handleSelfRoleChange("serviser")} disabled={loading}>Postavi na servisera</button>
          </div>
        </div>
      </div>

      <div className="row">
        <div className="col-md-6">
          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title">Dodaj novu osobu</h5>
              <form onSubmit={handleCreate}>
                <div className="mb-2">
                  <input className="form-control" placeholder="Ime" value={novo.ime} onChange={e=>setNovo({...novo, ime: e.target.value})} required />
                </div>
                <div className="mb-2">
                  <input className="form-control" placeholder="Prezime" value={novo.prezime} onChange={e=>setNovo({...novo, prezime: e.target.value})} required />
                </div>
                <div className="mb-2">
                  <input className="form-control" placeholder="Email" type="email" value={novo.email} onChange={e=>setNovo({...novo, email: e.target.value})} required />
                </div>
                <div className="mb-2">
                  <label className="form-label">Uloga</label>
                  <select className="form-select" value={novo.uloga} onChange={e=>setNovo({...novo, uloga: e.target.value})}>
                    <option value="korisnik">Korisnik</option>
                    <option value="serviser">Serviser</option>
                    <option value="administrator">Administrator</option>
                  </select>
                </div>
                <button className="btn btn-primary" disabled={loading}>{loading ? 'Spremanje...' : 'Dodaj'}</button>
              </form>
            </div>
          </div>

          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title">Korisnici</h5>
              {korisnici.length === 0 && <div className="text-muted">Nema korisnika</div>}
              <ul className="list-group mt-2">
                {korisnici.map(u => (
                  <li key={u.idOsoba} className="list-group-item d-flex justify-content-between align-items-center">
                    <div>
                      <div><strong>{u.imePrezime}</strong> <span className="text-muted">({u.email})</span></div>
                    </div>
                    <div>
                      <button className="btn btn-sm btn-outline-secondary me-2" onClick={()=>openEdit(u)}>Promijeni podatke</button>
                      <button className="btn btn-sm btn-danger" onClick={()=>handleDelete(u.idOsoba)}>Obriši</button>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title">Serviseri</h5>
              {serviseri.length === 0 && <div className="text-muted">Nema servisera</div>}
              <ul className="list-group mt-2">
                {serviseri.map(u => (
                  <li key={u.idOsoba} className="list-group-item d-flex justify-content-between align-items-center">
                    <div>
                      <div><strong>{u.imePrezime}</strong> <span className="text-muted">({u.email})</span></div>
                    </div>
                    <div>
                      <button className="btn btn-sm btn-outline-secondary me-2" onClick={()=>openEdit(u)}>Promijeni podatke</button>
                      <button className="btn btn-sm btn-danger" onClick={()=>handleDelete(u.idOsoba)}>Obriši</button>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>

      {editing && (
        <div className="modal show d-block" tabIndex={-1} role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Promijeni podatke</h5>
                <button type="button" className="btn-close" onClick={()=>setEditing(null)} aria-label="Close"></button>
              </div>
              <div className="modal-body">
                <div className="mb-2">
                  <input className="form-control" value={editing.ime} onChange={e=>setEditing({...editing, ime: e.target.value})} />
                </div>
                <div className="mb-2">
                  <input className="form-control" value={editing.prezime} onChange={e=>setEditing({...editing, prezime: e.target.value})} />
                </div>
                <div className="mb-2">
                  <input className="form-control" type="email" value={editing.email} onChange={e=>setEditing({...editing, email: e.target.value})} />
                </div>
                <div className="mb-2">
                  <label className="form-label">Uloga</label>
                  <select className="form-select" value={editing.uloga} onChange={e=>setEditing({...editing, uloga: e.target.value})}>
                    <option value="korisnik">Korisnik</option>
                    <option value="serviser">Serviser</option>
                    <option value="administrator">Administrator</option>
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={()=>setEditing(null)}>Odustani</button>
                <button type="button" className="btn btn-primary" onClick={handleSaveEdit}>Spremi</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Osobe;
