import { useEffect, useState } from "react";
import axios from "axios";
import "bootstrap/dist/css/bootstrap.min.css";

function Appointments({ user }) {
  const [termini, setTermini] = useState([]);
  const [serviseri, setServiseri] = useState([]);
  const [mojePrijave, setMojePrijave] = useState([]);
  const [dodijeljene, setDodijeljene] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // booking form state
  const [idVozilo, setIdVozilo] = useState("");
  const [selectedServiser, setSelectedServiser] = useState("");
  const [selectedTermin, setSelectedTermin] = useState("");
  const [napomena, setNapomena] = useState("");

  // edit owner modal
  const [editing, setEditing] = useState(null); // { idPrijava, ime, prezime, email }
  const [message, setMessage] = useState("");

  useEffect(() => {
    // fetch termini and serviseri
    axios.get("/api/appointments/termini").then(r => setTermini(r.data)).catch(e => console.error(e));
    axios.get("/api/appointments/serviseri").then(r => setServiseri(r.data)).catch(e => console.error(e));

    if (user) {
      // fetch my prijave
      axios.get("/api/appointments/prijave/moje").then(r => setMojePrijave(r.data)).catch(e => console.error(e));
    }

    // if serviser, fetch dodijeljene
    if (user && (user.uloga === "serviser" || user.uloga === "administrator")) {
      axios.get("/api/appointments/prijave/dodijeljene").then(r => setDodijeljene(r.data)).catch(e => console.error(e));
    }
  }, [user]);

  const handleBook = async (e) => {
    e.preventDefault();
    setLoading(true); setError(""); setMessage("");

    try {
      await axios.post("/api/appointments/prijave", {
        idVozilo: Number(idVozilo),
        idServiser: Number(selectedServiser),
        idTermin: Number(selectedTermin),
        napomenaVlasnika: napomena || null
      });

      setMessage("Prijava poslana.");
      setIdVozilo(""); setSelectedServiser(""); setSelectedTermin(""); setNapomena("");
      // refresh my prijave
      const r = await axios.get("/api/appointments/prijave/moje");
      setMojePrijave(r.data);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || "Greška pri slanju prijave.");
    } finally { setLoading(false); }
  };

  const openEdit = (p) => {
    setEditing({ idPrijava: p.idPrijava, ime: p.vlasnikInfo?.split(",")[0]?.split(" ")[0] || "", prezime: p.vlasnikInfo?.split(",")[0]?.split(" ").slice(1).join(" ") || "", email: p.vlasnikInfo?.split(",")[1]?.trim() || "" });
    setMessage(""); setError("");
  };

  const handleEditSave = async () => {
    try {
      await axios.patch(`/api/appointments/prijave/${editing.idPrijava}/vlasnik`, {
        ime: editing.ime || null,
        prezime: editing.prezime || null,
        email: editing.email || null
      });
      setMessage("Podaci su ažurirani.");
      setEditing(null);
      // refresh dodijeljene
      const r = await axios.get("/api/appointments/prijave/dodijeljene");
      setDodijeljene(r.data);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || "Greška pri ažuriranju.");
    }
  };

  return (
    <div className="container mt-5">
      <h2>Termini i prijave</h2>

      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row">
        <div className="col-md-6">
          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title">Rezerviraj termin</h5>
              <form onSubmit={handleBook}>
                <div className="mb-3">
                  <label className="form-label">Id vašeg vozila</label>
                  <input className="form-control" value={idVozilo} onChange={e=>setIdVozilo(e.target.value)} placeholder="npr. 1" required />
                </div>

                <div className="mb-3">
                  <label className="form-label">Odaberite servisa</label>
                  <select className="form-select" value={selectedServiser} onChange={e=>setSelectedServiser(e.target.value)} required>
                    <option value="">-- odaberite --</option>
                    {serviseri.map(s => <option key={s.idServiser} value={s.idServiser}>{s.imePrezime}</option>)}
                  </select>
                </div>

                <div className="mb-3">
                  <label className="form-label">Odaberite termin</label>
                  <select className="form-select" value={selectedTermin} onChange={e=>setSelectedTermin(e.target.value)} required>
                    <option value="">-- odaberite --</option>
                    {termini.map(t => <option key={t.idTermin} value={t.idTermin}>{new Date(t.datumVrijeme).toLocaleString()}</option>)}
                  </select>
                </div>

                <div className="mb-3">
                  <label className="form-label">Napomena</label>
                  <textarea className="form-control" value={napomena} onChange={e=>setNapomena(e.target.value)} rows={3}></textarea>
                </div>

                <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? "Slanje..." : "Pošalji prijavu"}</button>
              </form>
            </div>
          </div>

          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title">Moje prijave</h5>
              {mojePrijave.length === 0 && <div className="text-muted">Nema prijava</div>}
              <ul className="list-group mt-2">
                {mojePrijave.map(p => (
                  <li className="list-group-item" key={p.idPrijava}>
                    <div><strong>Status:</strong> {p.status}</div>
                    <div><strong>Termin:</strong> {p.datumTermina ? new Date(p.datumTermina).toLocaleString() : '-'}</div>
                    <div><strong>Vozilo:</strong> {p.voziloInfo}</div>
                    <div><strong>Serviser:</strong> {p.serviserIme}</div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>

        <div className="col-md-6">
          <div className="card mb-4">
            <div className="card-body">
              <h5 className="card-title">Prijave dodijeljene vama (samo za servisere)</h5>
              {dodijeljene.length === 0 && <div className="text-muted">Nema dodijeljenih prijava</div>}
              <ul className="list-group mt-2">
                {dodijeljene.map(p => (
                  <li className="list-group-item d-flex justify-content-between align-items-start" key={p.idPrijava}>
                    <div>
                      <div><strong>Termin:</strong> {p.datumTermina ? new Date(p.datumTermina).toLocaleString() : '-'}</div>
                      <div><strong>Vozilo:</strong> {p.voziloInfo}</div>
                      <div><strong>Vlasnik:</strong> {p.vlasnikInfo}</div>
                      <div><strong>Status:</strong> {p.status}</div>
                    </div>
                    <div>
                      <button className="btn btn-sm btn-outline-primary" onClick={()=>openEdit(p)}>Uredi podatke vlasnika</button>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Edit modal simple */}
      {editing && (
        <div className="modal show d-block" tabIndex={-1} role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Uredi podatke vlasnika</h5>
                <button type="button" className="btn-close" onClick={()=>setEditing(null)} aria-label="Close"></button>
              </div>
              <div className="modal-body">
                {error && <div className="alert alert-danger">{error}</div>}
                <div className="mb-3">
                  <label className="form-label">Ime</label>
                  <input className="form-control" value={editing.ime} onChange={e=>setEditing({...editing, ime: e.target.value})} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Prezime</label>
                  <input className="form-control" value={editing.prezime} onChange={e=>setEditing({...editing, prezime: e.target.value})} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Email</label>
                  <input className="form-control" value={editing.email} onChange={e=>setEditing({...editing, email: e.target.value})} />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={()=>setEditing(null)}>Odustani</button>
                <button type="button" className="btn btn-primary" onClick={handleEditSave}>Spremi</button>
              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default Appointments;
