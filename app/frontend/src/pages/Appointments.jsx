import { useEffect, useState } from "react";
import axios from "axios";
import "bootstrap/dist/css/bootstrap.min.css";

function Appointments({ user }) {
  const [termini, setTermini] = useState([]);
  const [serviseri, setServiseri] = useState([]);
  const [mojePrijave, setMojePrijave] = useState([]);
  const [dodijeljene, setDodijeljene] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [users, setUsers] = useState([]); // za admina: lista korisnika
  const [selectedUserForAdmin, setSelectedUserForAdmin] = useState("");
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

  // serviser edit modals
  const [editTerminModal, setEditTerminModal] = useState(null); // { idPrijava, newDatum }
  const [changeVoziloModal, setChangeVoziloModal] = useState(null); // { idPrijava, ownerId, currentVoziloId, selectedVozilo, vehicles: [] }
  const [zamjenaModal, setZamjenaModal] = useState(null); // { idPrijava, datumOd, datumDo, available: [], selectedZamjena }

  useEffect(() => {
    if (!user) {
      setServiseri([]); setMojePrijave([]); setDodijeljene([]);
      return;
    }

    // fetch serviseri
    axios.get("/api/appointments/serviseri").then(r => setServiseri(r.data)).catch(e => console.error(e));

    // fetch my prijave
    axios.get("/api/appointments/prijave/moje").then(r => setMojePrijave(r.data)).catch(e => console.error(e));

    // fetch my vehicles so user can pick which vehicle to service
    axios.get("/api/vehicles").then(r => setVehicles(r.data)).catch(e => console.error(e));

    // ako je admin, dohvati listu korisnika za upravljanje
    if (user && user.uloga === "administrator") {
      axios.get('/api/users')
        .then(r => setUsers(r.data))
        .catch(e => { console.error(e); setError('Greška pri dohvatu korisnika: ' + (e?.response?.data?.message || e.message)); });
    }

    // if serviser, fetch dodijeljene
    if (user && (user.uloga === "serviser" || user.uloga === "administrator")) {
      axios.get("/api/appointments/prijave/dodijeljene")
        .then(r => setDodijeljene(r.data))
        .catch(e => { console.error(e); setError('Greška pri dohvatu dodijeljenih prijava: ' + (e?.response?.data?.message || e.message)); });
    }
  }, [user]);

  useEffect(() => {
    // whenever serviser changes, fetch termini for that serviser (or all if none selected)
    if (!user) {
      setTermini([]);
      return;
    }

    if (selectedServiser) {
      axios.get(`/api/appointments/termini?serviserId=${selectedServiser}`).then(r => setTermini(r.data)).catch(e => console.error(e));
    } else {
      // if no serviser selected, clear or fetch all
      setTermini([]);
    }

    // ako je admin i izabran je korisnik za upravljanje, dohvati njegove vozila i prijave
    if (user && user.uloga === 'administrator' && selectedUserForAdmin) {
      axios.get(`/api/vehicles/for/${selectedUserForAdmin}`).then(r => setVehicles(r.data)).catch(e => console.error(e));
      axios.get(`/api/appointments/prijave/user?userId=${selectedUserForAdmin}`).then(r => setMojePrijave(r.data)).catch(e => console.error(e));
    }
  }, [selectedServiser, user]);

  const handleBook = async (e) => {
    e.preventDefault();
    if (!user) { setError("Molimo prijavite se prije rezervacije termina."); return; }
    setLoading(true); setError(""); setMessage("");

    try {
      if (user.uloga === 'administrator' && selectedUserForAdmin) {
        // create on behalf of selected user
        await axios.post(`/api/appointments/prijave/admin?ownerId=${selectedUserForAdmin}`, {
          idVozilo: Number(idVozilo),
          idServiser: Number(selectedServiser),
          idTermin: Number(selectedTermin),
          napomenaVlasnika: napomena || null
        });
        setMessage("Prijava kreirana za odabranog korisnika.");
        // refresh admin view of that user's prijave
        const r = await axios.get(`/api/appointments/prijave/user?userId=${selectedUserForAdmin}`);
        setMojePrijave(r.data);
      } else {
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
      }
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

  // Serviser: edit termin
  const openEditTermin = (p) => {
    const dt = p.datumTermina ? new Date(p.datumTermina) : null;
    const localVal = dt ? dt.toISOString().slice(0,16) : '';
    setEditTerminModal({ idPrijava: p.idPrijava, newDatum: localVal });
    setMessage(""); setError("");
  };

  const handleEditTerminSave = async () => {
    try {
      const id = editTerminModal.idPrijava;
      // newTerminDatum format: 'YYYY-MM-DDTHH:mm'
      await axios.put(`/api/prijave/${id}`, { newTerminDatum: editTerminModal.newDatum });
      setMessage('Termin je ažuriran.');
      setEditTerminModal(null);
      const r = await axios.get("/api/appointments/prijave/dodijeljene");
      setDodijeljene(r.data);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'Greška pri ažuriranju termina.');
    }
  };

  // Zamjensko vozilo: open modal and fetch available vehicles
  const openZamjena = async (p) => {
    const today = new Date().toISOString().slice(0,10);
    const next = new Date(); next.setDate(next.getDate() + 7);
    const nextStr = next.toISOString().slice(0,10);
    setZamjenaModal({ idPrijava: p.idPrijava, datumOd: today, datumDo: nextStr, available: [], selectedZamjena: null });
    try {
      const r = await axios.get(`/api/zamjene?from=${today}&to=${nextStr}`);
      setZamjenaModal(prev => ({ ...prev, available: r.data }));
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'Greška pri dohvatu zamjenskih vozila.');
    }
  };

  const handleReserveZamjena = async () => {
    try {
      await axios.post('/api/zamjene/rezervacije', {
        idPrijava: zamjenaModal.idPrijava,
        idZamjena: zamjenaModal.selectedZamjena,
        datumOd: zamjenaModal.datumOd,
        datumDo: zamjenaModal.datumDo
      });
      setMessage('Zamjensko vozilo rezervirano.');
      setZamjenaModal(null);
      // refresh relevant lists
      if (user && (user.uloga === 'serviser' || user.uloga === 'administrator')) {
        const r = await axios.get('/api/appointments/prijave/dodijeljene');
        setDodijeljene(r.data);
      }
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'Greška pri rezervaciji zamjenskog vozila.');
    }
  };

  // Serviser: change vehicle
  const openChangeVozilo = async (p) => {
    setChangeVoziloModal({ idPrijava: p.idPrijava, ownerId: p.idVlasnik, currentVoziloId: p.idVozilo, selectedVozilo: p.idVozilo, vehicles: [] });
    setMessage(""); setError("");
    try {
      const r = await axios.get(`/api/vehicles/for/${p.idVlasnik}`);
      setChangeVoziloModal(prev => ({ ...prev, vehicles: r.data }));
    } catch (err) {
      console.error(err);
      setError('Greška pri dohvatu vozila vlasnika.');
    }
  };

  const handleChangeVoziloSave = async () => {
    try {
      const id = changeVoziloModal.idPrijava;
      await axios.patch(`/api/appointments/prijave/${id}/vozilo`, { idVozilo: Number(changeVoziloModal.selectedVozilo) });
      setMessage('Vozilo je promijenjeno.');
      setChangeVoziloModal(null);
      const r = await axios.get("/api/appointments/prijave/dodijeljene");
      setDodijeljene(r.data);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'Greška pri promjeni vozila.');
    }
  };

  if (!user) {
    return (
      <div className="container mt-5 text-center">
        <h4>Molimo prijavite se kako biste vidjeli termine i rezervirali termin.</h4>
      </div>
    );
  }

  return (
    <div className="container mt-5">
      <h2>Termini i prijave</h2>

      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row">
        {user && user.uloga === 'korisnik' ? (
          <div className="col-md-12">
            <div className="card mb-4">
              <div className="card-body">
                <h5 className="card-title">Rezerviraj termin</h5>
                {user && user.uloga === "administrator" && (
                  <div className="mb-2">
                    <button className="btn btn-sm btn-outline-secondary" onClick={async (e) => {
                      e.preventDefault();
                      try {
                        await axios.post('/api/debug/seed-test');
                        // refresh data
                        axios.get('/api/appointments/serviseri').then(r => setServiseri(r.data)).catch(e => console.error(e));
                        axios.get('/api/vehicles').then(r => setVehicles(r.data)).catch(e => console.error(e));
                        alert('Seed završen. Provjeri dropdown.');
                      } catch (err) {
                        console.error(err);
                        alert('Seed nije uspio. Pogledaj konzolu.');
                      }
                    }}>Seed test data</button>
                  </div>
                )}

                {user && user.uloga === 'administrator' && (
                  <div className="mb-3">
                    <label className="form-label">Odaberite korisnika (admin)</label>
                    <select className="form-select" value={selectedUserForAdmin} onChange={e=>setSelectedUserForAdmin(e.target.value)}>
                      <option value="">-- odaberite korisnika --</option>
                      {users.map(u => <option key={u.idOsoba} value={u.idOsoba}>{u.imePrezime} ({u.email})</option>)}
                    </select>
                  </div>
                )}

                <form onSubmit={handleBook}>
                  <div className="mb-3">
                    <label className="form-label">Odaberite vozilo</label>
                    <select className="form-select" value={idVozilo} onChange={e=>setIdVozilo(e.target.value)} required>
                      <option value="">-- odaberite vozilo --</option>
                      {vehicles.map(v => <option key={v.id_vozilo} value={v.id_vozilo}>{v.registracija} ({v.model_naziv || v.marka_naziv || ''})</option>)}
                    </select>
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
          </div>
        ) : user && user.uloga === 'serviser' ? (
          <div className="col-md-12">
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
                      <div className="d-flex gap-2">
                        <button className="btn btn-sm btn-outline-primary" onClick={()=>openEdit(p)}>Uredi podatke vlasnika</button>
                        <button className="btn btn-sm btn-outline-secondary" onClick={()=>openEditTermin(p)}>Uredi termin</button>
                        <button className="btn btn-sm btn-outline-success" onClick={()=>openChangeVozilo(p)}>Promijeni vozilo</button>
                        <button className="btn btn-sm btn-outline-secondary" onClick={()=>openZamjena(p)}>Rezerviraj zamjensko vozilo</button>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        ) : (
          // default admin / mixed view
          <>
            <div className="col-md-6">
              <div className="card mb-4">
                <div className="card-body">
                  <h5 className="card-title">Rezerviraj termin</h5>
                  {user && user.uloga === "administrator" && (
                    <div className="mb-2">
                      <button className="btn btn-sm btn-outline-secondary" onClick={async (e) => {
                        e.preventDefault();
                        try {
                          await axios.post('/api/debug/seed-test');
                          // refresh data
                          axios.get('/api/appointments/serviseri').then(r => setServiseri(r.data)).catch(e => console.error(e));
                          axios.get('/api/vehicles').then(r => setVehicles(r.data)).catch(e => console.error(e));
                          alert('Seed završen. Provjeri dropdown.');
                        } catch (err) {
                          console.error(err);
                          alert('Seed nije uspio. Pogledaj konzolu.');
                        }
                      }}>Seed test data</button>
                    </div>
                  )}

                  {user && user.uloga === 'administrator' && (
                    <div className="mb-3">
                      <label className="form-label">Odaberite korisnika (admin)</label>
                      <select className="form-select" value={selectedUserForAdmin} onChange={e=>setSelectedUserForAdmin(e.target.value)}>
                        <option value="">-- odaberite korisnika --</option>
                        {users.map(u => <option key={u.idOsoba} value={u.idOsoba}>{u.imePrezime} ({u.email})</option>)}
                      </select>
                    </div>
                  )}
                  <form onSubmit={handleBook}>
                    <div className="mb-3">
                      <label className="form-label">Odaberite vozilo</label>
                      <select className="form-select" value={idVozilo} onChange={e=>setIdVozilo(e.target.value)} required>
                        <option value="">-- odaberite vozilo --</option>
                        {vehicles.map(v => <option key={v.id_vozilo} value={v.id_vozilo}>{v.registracija} ({v.model_naziv || v.marka_naziv || ''})</option>)}
                      </select>
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
                      <li className="list-group-item d-flex justify-content-between align-items-start" key={p.idPrijava}>
                        <div>
                          <div><strong>Status:</strong> {p.status}</div>
                          <div><strong>Termin:</strong> {p.datumTermina ? new Date(p.datumTermina).toLocaleString() : '-'}</div>
                          <div><strong>Vozilo:</strong> {p.voziloInfo}</div>
                          <div><strong>Serviser:</strong> {p.serviserIme}</div>
                        </div>
                        <div>
                          {user && (
                            <>
                              <button className="btn btn-sm btn-danger me-2" onClick={async ()=>{
                                if (!window.confirm('Obrisati ovu prijavu?')) return;
                                try {
                                  await axios.delete(`/api/appointments/prijave/${p.idPrijava}`);
                                  // refresh list
                                  if (selectedUserForAdmin) {
                                    const r = await axios.get(`/api/appointments/prijave/user?userId=${selectedUserForAdmin}`);
                                    setMojePrijave(r.data);
                                  } else {
                                    const r = await axios.get(`/api/appointments/prijave/moje`);
                                    setMojePrijave(r.data);
                                  }
                                } catch (err) {
                                  console.error(err);
                                  alert('Greška pri brisanju prijave');
                                }
                              }}>Obriši</button>
                              <button className="btn btn-sm btn-outline-secondary" onClick={()=>openZamjena(p)}>Rezerviraj zamjensko vozilo</button>
                            </>
                          )}
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
          </>
        )}
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

      {/* Serviser: edit termin modal */}
      {editTerminModal && (
        <div className="modal show d-block" tabIndex={-1} role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Uredi termin</h5>
                <button type="button" className="btn-close" onClick={()=>setEditTerminModal(null)} aria-label="Close"></button>
              </div>
              <div className="modal-body">
                {error && <div className="alert alert-danger">{error}</div>}
                <div className="mb-3">
                  <label className="form-label">Novi termin</label>
                  <input type="datetime-local" className="form-control" value={editTerminModal.newDatum} onChange={e=>setEditTerminModal({...editTerminModal, newDatum: e.target.value})} />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={()=>setEditTerminModal(null)}>Odustani</button>
                <button type="button" className="btn btn-primary" onClick={handleEditTerminSave}>Spremi</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Serviser: change vehicle modal */}
      {changeVoziloModal && (
        <div className="modal show d-block" tabIndex={-1} role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Promijeni vozilo</h5>
                <button type="button" className="btn-close" onClick={()=>setChangeVoziloModal(null)} aria-label="Close"></button>
              </div>
              <div className="modal-body">
                {error && <div className="alert alert-danger">{error}</div>}
                <div className="mb-3">
                  <label className="form-label">Odaberite vozilo vlasnika</label>
                  <select className="form-select" value={changeVoziloModal.selectedVozilo} onChange={e=>setChangeVoziloModal({...changeVoziloModal, selectedVozilo: e.target.value})}>
                    <option value="">-- odaberite --</option>
                    {(changeVoziloModal.vehicles || []).map(v => <option key={v.id_vozilo} value={v.id_vozilo}>{v.registracija} ({v.model_naziv || v.marka_naziv || ''})</option>)}
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={()=>setChangeVoziloModal(null)}>Odustani</button>
                <button type="button" className="btn btn-primary" onClick={handleChangeVoziloSave}>Spremi</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Zamjensko vozilo modal */}
      {zamjenaModal && (
        <div className="modal show d-block" tabIndex={-1} role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Rezerviraj zamjensko vozilo</h5>
                <button type="button" className="btn-close" onClick={()=>setZamjenaModal(null)} aria-label="Close"></button>
              </div>
              <div className="modal-body">
                {error && <div className="alert alert-danger">{error}</div>}
                <div className="mb-3">
                  <label className="form-label">Datum od</label>
                  <input type="date" className="form-control" value={zamjenaModal.datumOd} onChange={e => setZamjenaModal({...zamjenaModal, datumOd: e.target.value})} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Datum do</label>
                  <input type="date" className="form-control" value={zamjenaModal.datumDo} onChange={e => setZamjenaModal({...zamjenaModal, datumDo: e.target.value})} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Dostupna vozila</label>
                  <select className="form-select" value={zamjenaModal.selectedZamjena || ''} onChange={e => setZamjenaModal({...zamjenaModal, selectedZamjena: e.target.value})}>
                    <option value="">-- odaberite zamjensko vozilo --</option>
                    {(zamjenaModal.available || []).map(z => <option key={z.id_zamjena} value={z.id_zamjena}>{z.registracija} ({z.model?.naziv || ''})</option>)}
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={()=>setZamjenaModal(null)}>Odustani</button>
                <button type="button" className="btn btn-primary" onClick={handleReserveZamjena}>Rezerviraj</button>
              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default Appointments;
