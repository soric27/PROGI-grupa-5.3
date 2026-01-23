import { useEffect, useState } from "react";
import axios from "axios";
import "bootstrap/dist/css/bootstrap.min.css";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

function Appointments({ user }) {
  const renderStatus = (status) => {
    const s = (status || "").toLowerCase();
    if (s.includes("obradi")) return <span className="badge bg-warning text-dark">U obradi</span>;
    if (s.includes("zavr")) return <span className="badge bg-success">ZavrĹˇeno</span>;
    if (s.includes("odgod")) return <span className="badge bg-secondary">OdgoÄ‘eno</span>;
    if (s) return <span className="badge bg-info text-dark">{status}</span>;
    return <span className="text-muted">-</span>;
  };
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
  const [selectedDatum, setSelectedDatum] = useState(null);
  const [napomena, setNapomena] = useState("");
  const [selectedKvarovi, setSelectedKvarovi] = useState([]); // list of selected kvar IDs
  const [kvarovi, setKvarovi] = useState([]); // list of all available kvarovi

  // replacement vehicle request
  const [zamjenaRequested, setZamjenaRequested] = useState(false);
  const [zamjenaOd, setZamjenaOd] = useState(""); // YYYY-MM-DD
  const [zamjenaDo, setZamjenaDo] = useState("");
  const [availableZamjene, setAvailableZamjene] = useState([]);
  const [selectedZamjenaId, setSelectedZamjenaId] = useState("");

  // edit owner modal
  const [editing, setEditing] = useState(null); // { idPrijava, ime, prezime, email }
  const [message, setMessage] = useState("");

  // serviser edit modals
  const [editTerminModal, setEditTerminModal] = useState(null); // { idPrijava, newDatum }
  const [changeVoziloModal, setChangeVoziloModal] = useState(null); // { idPrijava, ownerId, currentVoziloId, selectedVozilo, vehicles: [] }
  const [zamjenaModal, setZamjenaModal] = useState(null); // { idPrijava, datumOd, datumDo, available: [], selectedZamjena }
  const [statusModal, setStatusModal] = useState(null); // { idPrijava, noviStatus }
  const [noteModal, setNoteModal] = useState(null); // { idPrijava, opis }

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

    // fetch available kvarovi (defects)
    axios.get("/api/kvarovi").then(r => setKvarovi(r.data)).catch(e => console.error(e));

    // ako je admin, dohvati listu korisnika za upravljanje
    if (user && user.uloga === "administrator") {
      axios.get('/api/users')
        .then(r => setUsers(r.data))
        .catch(e => { console.error(e); setError('GreĹˇka pri dohvatu korisnika: ' + (e?.response?.data?.message || e.message)); });
    }

    // if serviser, fetch dodijeljene
    if (user && (user.uloga === "serviser" || user.uloga === "administrator")) {
      axios.get("/api/appointments/prijave/dodijeljene")
        .then(r => setDodijeljene(r.data))
        .catch(e => { console.error(e); setError('GreĹˇka pri dohvatu dodijeljenih prijava: ' + (e?.response?.data?.message || e.message)); });
    }
  }, [user]);

  useEffect(() => {
    setSelectedDatum(null);
    setSelectedTermin("");
  }, [selectedServiser]);

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

    // fetch available replacement vehicles when user chooses a date range
    if (zamjenaOd && zamjenaDo) {
      axios.get(`/api/zamjene?from=${zamjenaOd}&to=${zamjenaDo}`).then(r => setAvailableZamjene(r.data)).catch(e => console.error(e));
    } else {
      setAvailableZamjene([]);
    }

    // ako je admin i izabran je korisnik za upravljanje, dohvati njegove vozila i prijave
    if (user && user.uloga === 'administrator' && selectedUserForAdmin) {
      axios.get(`/api/vehicles/for/${selectedUserForAdmin}`).then(r => setVehicles(r.data)).catch(e => console.error(e));
      axios.get(`/api/appointments/prijave/user?userId=${selectedUserForAdmin}`).then(r => setMojePrijave(r.data)).catch(e => console.error(e));
    }
  }, [selectedServiser, user, zamjenaOd, zamjenaDo, selectedUserForAdmin]);

  // Poll 'moje prijave' for korisnik every 10s so serviser notes appear without manual refresh
  useEffect(() => {
    if (!user || user.uloga !== 'korisnik') return;
    const id = setInterval(async () => {
      try {
        const r = await axios.get('/api/appointments/prijave/moje');
        setMojePrijave(r.data);
      } catch (e) {
        console.error(e);
      }
    }, 10000);
    return () => clearInterval(id);
  }, [user]);

  const handleBook = async (e) => {
    e.preventDefault();
    if (!user) { setError("Molimo prijavite se prije rezervacije termina."); return; }
    if (!selectedTermin) { setError("Molimo odaberite termin."); return; }
    if (!selectedKvarovi || selectedKvarovi.length === 0) { setError("Potrebno je odabrati barem jedan kvar."); return; }
    if (selectedDatum && !availableDates.includes(toDateString(selectedDatum))) { setError("Za odabrani dan nema dostupnih termina."); return; }
    if (zamjenaRequested) {
      if (!zamjenaOd || !zamjenaDo) { setError("Molimo odaberite raspon datuma za zamjensko vozilo."); return; }
      if (zamjenaOd < minZamjenaDate || zamjenaDo < minZamjenaDate) { setError("Datumi za zamjensko vozilo ne mogu biti prije današnjeg datuma ili termina."); return; }
      if (zamjenaDo < zamjenaOd) { setError("Datum 'do' ne može biti prije datuma 'od'."); return; }
    }
    setLoading(true); setError(""); setMessage("");

    try {
      if (user.uloga === 'administrator' && selectedUserForAdmin) {
        // create on behalf of selected user
        await axios.post(`/api/appointments/prijave/admin?ownerId=${selectedUserForAdmin}`, {
          idVozilo: Number(idVozilo),
          idServiser: Number(selectedServiser),
          idTermin: Number(selectedTermin),
          napomenaVlasnika: napomena || null,
          idZamjena: selectedZamjenaId ? Number(selectedZamjenaId) : null,
          datumOd: zamjenaOd || null,
          datumDo: zamjenaDo || null,
          idKvarovi: selectedKvarovi.length > 0 ? selectedKvarovi.map(Number) : []
        });
        setMessage("Prijava kreirana za odabranog korisnika.");
        // refresh admin view of that user's prijave
        const r = await axios.get(`/api/appointments/prijave/user?userId=${selectedUserForAdmin}`);
        setMojePrijave(r.data);
        // OsvjeĹľi dostupne termine nakon booking-a
        if (selectedServiser) {
          axios.get(`/api/appointments/termini?serviserId=${selectedServiser}`).then(r => setTermini(r.data)).catch(e => console.error(e));
        }
        // reset form
        setIdVozilo(''); setSelectedServiser(''); setSelectedTermin(''); setSelectedDatum(null); setNapomena(''); setSelectedKvarovi([]);
        setZamjenaRequested(false); setZamjenaOd(''); setZamjenaDo(''); setSelectedZamjenaId(''); setAvailableZamjene([]);
      } else {
        await axios.post("/api/appointments/prijave", {
          idVozilo: Number(idVozilo),
          idServiser: Number(selectedServiser),
          idTermin: Number(selectedTermin),
          napomenaVlasnika: napomena || null,
          idZamjena: selectedZamjenaId ? Number(selectedZamjenaId) : null,
          datumOd: zamjenaOd || null,
          datumDo: zamjenaDo || null,
          idKvarovi: selectedKvarovi.length > 0 ? selectedKvarovi.map(Number) : []
        });

        setMessage("Prijava poslana.");
        setIdVozilo(""); setSelectedServiser(""); setSelectedTermin(""); setSelectedDatum(null); setNapomena(""); setSelectedKvarovi([]);
        // refresh my prijave
        const r = await axios.get("/api/appointments/prijave/moje");
        setMojePrijave(r.data);
        // OsvjeĹľi dostupne termine nakon booking-a
        if (selectedServiser) {
          axios.get(`/api/appointments/termini?serviserId=${selectedServiser}`).then(r => setTermini(r.data)).catch(e => console.error(e));
        }
        // reset zamjena inputs
        setZamjenaRequested(false); setZamjenaOd(''); setZamjenaDo(''); setSelectedZamjenaId(''); setAvailableZamjene([]);
      }
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || "GreĹˇka pri slanju prijave.");
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
      setMessage("Podaci su aĹľurirani.");
      setEditing(null);
      // refresh dodijeljene
      const r = await axios.get("/api/appointments/prijave/dodijeljene");
      setDodijeljene(r.data);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || "GreĹˇka pri aĹľuriranju.");
    }
  };

  // Serviser: edit termin
  const openEditTermin = async (p) => {
    const modal = { idPrijava: p.idPrijava, available: [], selectedSlot: '', currentTermin: p.datumTermina };
    setMessage(""); setError("");
    try {
      // Dohvati slobodne termine za tog servisera (isti kao Ĺˇto korisnik vidi)
      if (p.idServiser) {
        const r = await axios.get(`/api/appointments/termini?serviserId=${p.idServiser}`);
        if (modal.currentTermin) {
          const current = new Date(modal.currentTermin).getTime();
          modal.available = r.data.filter(t => new Date(t.datumVrijeme).getTime() > current);
        } else {
          modal.available = r.data;
        }
      }
    } catch (e) {
      console.error('Ne mogu dohvatiti termine servisa', e);
    }
    setEditTerminModal(modal);
  };

  const handleEditTerminSave = async () => {
    try {
      if (!editTerminModal.selectedSlot) {
        setError('Molimo odaberite novi termin.');
        return;
      }
      const id = editTerminModal.idPrijava;
      // selectedSlot je ISO datetime format
      await axios.put(`/api/prijave/${id}`, { newTerminDatum: editTerminModal.selectedSlot });
      setMessage('Termin je aĹľuriran.');
      setEditTerminModal(null);
      
      // OsvjeĹľi sve relevantne liste
      const r = await axios.get("/api/appointments/prijave/dodijeljene");
      setDodijeljene(r.data);
      
      // OsvjeĹľi i korisniku njegovu listu ako se termin promijenio
      const myApps = await axios.get("/api/appointments/prijave/moje");
      setMojePrijave(myApps.data);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'GreĹˇka pri aĹľuriranju termina.');
    }
  };

  const handleStatusSave = async () => {
    try {
      const id = statusModal.idPrijava;
      await axios.patch(`/api/appointments/prijave/${id}/status`, { noviStatus: statusModal.noviStatus });
      setMessage('Status je aĹľuriran.');
      setStatusModal(null);
      const r = await axios.get('/api/appointments/prijave/dodijeljene');
      setDodijeljene(r.data);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'GreĹˇka pri aĹľuriranju statusa.');
    }
  };
  const handleCompletePrijava = async (idPrijava) => {
    if (!window.confirm('OznaÄŤiti servis zavrĹˇenim i obrisati prijavu?')) return;
    try {
      await axios.post(`/api/appointments/prijave/${idPrijava}/complete`);
      setMessage('Servis je zavrĹˇen. Prijava je obrisana i korisnik je obavijeĹˇten.');
      const r = await axios.get('/api/appointments/prijave/dodijeljene');
      setDodijeljene(r.data);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'GreĹˇka pri zavrĹˇetku servisa.');
    }
  };


  const handleAddNote = async () => {
    try {
      const id = noteModal.idPrijava;
      await axios.post(`/api/appointments/prijave/${id}/napomene`, { opis: noteModal.opis });
      setMessage('Napomena dodana.');
      setNoteModal(null);
      const r = await axios.get('/api/appointments/prijave/dodijeljene');
      setDodijeljene(r.data);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'GreĹˇka pri dodavanju napomene.');
    }
  };

  const downloadObrazac = async (idPrijava, tip) => {
    try {
      const resp = await axios.post(
        `/api/appointments/prijave/${idPrijava}/${tip}`,
        {},
        { responseType: "blob" }
      );
      const blob = new Blob([resp.data], { type: "application/pdf" });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `obrazac_${tip}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || "GreĹˇka pri generiranju obrasca.");
    }
  };

  // Zamjensko vozilo: open modal and fetch available vehicles
  const openZamjena = async (p) => {
    const todayObj = new Date();
    const terminObj = p?.datumTermina ? new Date(p.datumTermina) : null;
    const minDateObj = (terminObj && terminObj > todayObj) ? terminObj : todayObj;
    const minDate = minDateObj.toISOString().slice(0, 10);
    const next = new Date(minDateObj);
    next.setDate(next.getDate() + 7);
    const nextStr = next.toISOString().slice(0, 10);
    setZamjenaModal({ idPrijava: p.idPrijava, datumOd: minDate, datumDo: nextStr, available: [], selectedZamjena: null, minDate });
    try {
      const r = await axios.get(`/api/zamjene?from=${minDate}&to=${nextStr}`);
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
        idZamjena: Number(zamjenaModal.selectedZamjena),
        datumOd: zamjenaModal.datumOd,
        datumDo: zamjenaModal.datumDo
      });
      setMessage('Zamjensko vozilo rezervirano.');
      setZamjenaModal(null);
      // refresh relevant lists
      if (user && user.uloga === 'korisnik') {
        const r = await axios.get('/api/appointments/prijave/moje');
        setMojePrijave(r.data);
      } else if (user && (user.uloga === 'serviser' || user.uloga === 'administrator')) {
        const r = await axios.get('/api/appointments/prijave/dodijeljene');
        setDodijeljene(r.data);
      }
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'GreĹˇka pri rezervaciji zamjenskog vozila.');
    }
  };

  // Serviser: change vehicle
  const openChangeVozilo = async (p) => {
    setZamjenaModal({ idPrijava: p.idPrijava, datumOd: new Date().toISOString().slice(0,10), datumDo: new Date(new Date().setDate(new Date().getDate()+7)).toISOString().slice(0,10), available: [], selectedZamjena: null });
    setMessage(""); setError("");
    try {
      const from = new Date().toISOString().slice(0,10);
      const to = new Date(new Date().setDate(new Date().getDate()+7)).toISOString().slice(0,10);
      const r = await axios.get(`/api/zamjene?from=${from}&to=${to}`);
      setZamjenaModal(prev => ({ ...prev, available: r.data }));
    } catch (err) {
      console.error(err);
      setError('GreĹˇka pri dohvatu zamjenskih vozila.');
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
      setError(err?.response?.data?.message || 'GreĹˇka pri promjeni vozila.');
    }
  };

  const availableDates = Array.from(new Set(
    termini
      .map(t => (t && t.datumVrijeme ? t.datumVrijeme.slice(0, 10) : null))
      .filter(Boolean)
  )).sort();
  const availableDateObjects = availableDates.map(d => new Date(`${d}T00:00:00`));
  const minAvailableDate = availableDates.length > 0 ? availableDates[0] : "";
  const maxAvailableDate = availableDates.length > 0 ? availableDates[availableDates.length - 1] : "";
  const minDateObj = minAvailableDate ? new Date(`${minAvailableDate}T00:00:00`) : null;
  const maxDateObj = maxAvailableDate ? new Date(`${maxAvailableDate}T00:00:00`) : null;

  const toDateString = (d) => {
    if (!d) return "";
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const formatZamjenaLabel = (z) => {
    const marka = z?.marka_naziv || z?.markaNaziv || z?.model?.marka?.naziv || "";
    const model = z?.model_naziv || z?.modelNaziv || z?.model?.naziv || "";
    const info = [marka, model].filter(Boolean).join(" ");
    return `${z?.registracija || ""}${info ? ` (${info})` : ""}`;
  };
  const todayStr = new Date().toISOString().slice(0, 10);
  const selectedDateStr = selectedDatum ? toDateString(selectedDatum) : "";
  const minZamjenaDate = selectedDateStr && selectedDateStr > todayStr ? selectedDateStr : todayStr;
  const timeSlotsForDate = (date) => (
    termini
      .filter(t => t && t.datumVrijeme && t.datumVrijeme.slice(0, 10) === date)
      .sort((a, b) => new Date(a.datumVrijeme) - new Date(b.datumVrijeme))
  );

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
                        alert('Seed zavrĹˇen. Provjeri dropdown.');
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
                    <label className="form-label">Odaberite servisera</label>
                    <select className="form-select" value={selectedServiser} onChange={e=>setSelectedServiser(e.target.value)} required>
                      <option value="">-- odaberite --</option>
                      {serviseri.map(s => <option key={s.idServiser} value={s.idServiser}>{s.imePrezime}</option>)}
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Odaberite dan</label>
                    <DatePicker
                      selected={selectedDatum}
                      onChange={(date) => {
                        if (!date) {
                          setSelectedDatum(null);
                          setSelectedTermin("");
                          return;
                        }
                        const dateStr = toDateString(date);
                        if (!availableDates.includes(dateStr)) {
                          setSelectedDatum(null);
                          setSelectedTermin("");
                          setError("Za odabrani dan nema dostupnih termina.");
                          return;
                        }
                        setError("");
                        setSelectedDatum(date);
                        setSelectedTermin("");
                      }}
                      className="form-control"
                      placeholderText="Odaberite dan"
                      includeDates={availableDateObjects}
                      minDate={minDateObj}
                      maxDate={maxDateObj}
                      disabled={availableDates.length === 0}
                      dateFormat="dd.MM.yyyy"
                    />
                    {selectedDatum && !availableDates.includes(toDateString(selectedDatum)) && (
                      <div className="text-danger small mt-1">Za odabrani dan nema termina.</div>
                    )}
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Odaberite termin</label>
                    {selectedDatum ? (
                      <div className="border rounded p-2 bg-light">
                        <div className="d-flex flex-wrap gap-2">
                          {timeSlotsForDate(toDateString(selectedDatum)).map(t => (
                            <button
                              key={t.idTermin}
                              type="button"
                              className={`btn btn-sm ${String(selectedTermin) === String(t.idTermin) ? "btn-primary" : "btn-outline-primary"}`}
                              onClick={() => setSelectedTermin(String(t.idTermin))}
                            >
                              {new Date(t.datumVrijeme).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                            </button>
                          ))}
                          {timeSlotsForDate(toDateString(selectedDatum)).length === 0 && (
                            <div className="text-muted">Nema termina za odabrani dan.</div>
                          )}
                        </div>
                      </div>
                    ) : (
                      <div className="text-muted">Prvo odaberite dan u kalendaru.</div>
                    )}
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Odaberite kvarove (defekte)</label>
                    <div className="border rounded p-2">
                      {kvarovi.length === 0 ? (
                        <div className="text-muted">Nema dostupnih kvarova</div>
                      ) : (
                        kvarovi.map(kvar => (
                          <div key={kvar.id_kvar} className="form-check">
                            <input
                              className="form-check-input"
                              type="checkbox"
                              id={`kvar-${kvar.id_kvar}`}
                              checked={selectedKvarovi.includes(kvar.id_kvar)}
                              onChange={e => {
                                if (e.target.checked) {
                                  setSelectedKvarovi([...selectedKvarovi, kvar.id_kvar]);
                                } else {
                                  setSelectedKvarovi(selectedKvarovi.filter(k => k !== kvar.id_kvar));
                                }
                              }}
                            />
                            <label className="form-check-label" htmlFor={`kvar-${kvar.id_kvar}`}>
                              <strong>{kvar.naziv}</strong> {kvar.opis && `- ${kvar.opis}`}
                            </label>
                          </div>
                        ))
                      )}
                    </div>
                  </div>

                  <div className="mb-3 form-check">
                    <input className="form-check-input" type="checkbox" id="zamjenaCheck" checked={zamjenaRequested} onChange={e => {
                      const checked = e.target.checked;
                      setZamjenaRequested(checked);
                      if (!checked) { setZamjenaOd(''); setZamjenaDo(''); setAvailableZamjene([]); setSelectedZamjenaId(''); }
                      else { // if enabling but no date range chosen, show all currently available zamjena
                        if (!zamjenaOd || !zamjenaDo) {
                          axios.get('/api/zamjene').then(r => setAvailableZamjene(r.data)).catch(e => console.error(e));
                        }
                      }
                    }} />
                    <label className="form-check-label" htmlFor="zamjenaCheck">TraĹľim zamjensko vozilo</label>
                  </div>

                  {zamjenaRequested && (
                    <div className="border rounded p-3 mb-3">
                      <div className="row g-2">
                        <div className="col-md-4">
                          <label className="form-label">Datum od</label>
                          <input type="date" className="form-control" value={zamjenaOd} min={minZamjenaDate} onChange={e => setZamjenaOd(e.target.value)} />
                        </div>
                        <div className="col-md-4">
                          <label className="form-label">Datum do</label>
                          <input type="date" className="form-control" value={zamjenaDo} min={zamjenaOd || minZamjenaDate} onChange={e => setZamjenaDo(e.target.value)} />
                        </div>
                        <div className="col-md-4">
                          <label className="form-label">Odaberite zamjensko vozilo</label>
                          <select className="form-select" value={selectedZamjenaId} onChange={e => setSelectedZamjenaId(e.target.value)}>
                            <option value="">-- odaberite zamjensko vozilo --</option>
                            {availableZamjene.map(z => <option key={z.id_zamjena ?? z.idZamjena} value={z.id_zamjena ?? z.idZamjena}>{formatZamjenaLabel(z)}</option>)}
                          </select>
                        </div>
                      </div>
                    </div>
                  )}

                  <div className="mb-3">
                    <label className="form-label">Napomena</label>
                    <textarea className="form-control" value={napomena} onChange={e=>setNapomena(e.target.value)} rows={3}></textarea>
                  </div>

                  <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? "Slanje..." : "PoĹˇalji prijavu"}</button>
                </form>
              </div>
            </div>

            <div className="card mb-4">
              <div className="card-body">
                <div className="d-flex align-items-center justify-content-between">
                  <h5 className="card-title mb-0">Moje prijave</h5>
                  <button className="btn btn-sm btn-outline-secondary" onClick={async ()=>{ const r = await axios.get(`/api/appointments/prijave/moje`); setMojePrijave(r.data); }}>OsvjeĹľi</button>
                </div>
                {mojePrijave.length === 0 && <div className="text-muted">Nema prijava</div>}
                <ul className="list-group mt-2">
                  {mojePrijave.map(p => (
                    <li className="list-group-item d-flex justify-content-between align-items-start" key={p.idPrijava}>
                      <div>
                        <div><strong>Status:</strong> {renderStatus(p.status)}</div>
                        <div><strong>Termin:</strong> {p.datumTermina ? new Date(p.datumTermina).toLocaleString() : '-'}</div>
                        <div><strong>Vozilo:</strong> {p.voziloInfo}</div>
                        <div><strong>Serviser:</strong> {p.serviserIme}</div>
                        <div className="mt-2"><strong>Napomena vlasnika:</strong> {p.napomenaVlasnika || <span className="text-muted">-</span>}</div>
                        <div><strong>Napomena servisera:</strong> {p.napomeneServisera && p.napomeneServisera.length ? p.napomeneServisera[0].opis : <span className="text-muted">-</span>}</div>
                        <div className="mt-1"><strong>Zamjensko vozilo:</strong> {p.rezervacijaZamjene ? `${p.rezervacijaZamjene.registracija} (${p.rezervacijaZamjene.datumOd} â€” ${p.rezervacijaZamjene.datumDo})` : <span className="text-muted">-</span>}</div>
                        {p.kvarovi && p.kvarovi.length > 0 && (
                          <div className="mt-2">
                            <strong>Kvarovi:</strong>
                            <ul className="mb-0 ms-3">
                              {p.kvarovi.map(kv => (
                                <li key={kv.id_kvar}>{kv.naziv} {kv.opis && `- ${kv.opis}`}</li>
                              ))}
                            </ul>
                          </div>
                        )}
                      </div>
                      <div>
                        {user && (
                          <>
                            <button className="btn btn-sm btn-danger me-2" onClick={async ()=>{
                              if (!window.confirm('Obrisati ovu prijavu?')) return;
                              try {
                                await axios.delete(`/api/appointments/prijave/${p.idPrijava}`);
                                // refresh list
                                const r = await axios.get(`/api/appointments/prijave/moje`);
                                setMojePrijave(r.data);
                              } catch (err) {
                                console.error(err);
                                alert('GreĹˇka pri brisanju prijave');
                              }
                            }}>ObriĹˇi</button>
                            {!p.rezervacijaZamjene && (user.idOsoba === p.idVlasnik || user.uloga === 'administrator') && (
                              <button className="btn btn-sm btn-outline-secondary" onClick={()=>openZamjena(p)}>Rezerviraj zamjensko vozilo</button>
                            )}
                          </>
                        )}
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        ) : user && user.uloga === 'serviser' ? (
          <div className="col-md-12">
            <div className="card mb-4">
              <div className="card-body">
                <h5 className="card-title">Prijave dodijeljene vama</h5>
                {dodijeljene.length === 0 && <div className="text-muted">Nema dodijeljenih prijava</div>}
                <ul className="list-group mt-2">
                  {dodijeljene.map(p => (
                    <li className="list-group-item d-flex justify-content-between align-items-start" key={p.idPrijava}>
                      <div>
                        <div><strong>Termin:</strong> {p.datumTermina ? new Date(p.datumTermina).toLocaleString() : '-'}</div>
                        <div><strong>Vozilo:</strong> {p.voziloInfo}</div>
                        <div><strong>Vlasnik:</strong> {p.vlasnikInfo}</div>
                        <div><strong>Status:</strong> {renderStatus(p.status)}</div>
                        <div className="mt-2"><strong>Napomena vlasnika:</strong> {p.napomenaVlasnika || <span className="text-muted">-</span>}</div>
                        <div><strong>Napomena servisera:</strong> {p.napomeneServisera && p.napomeneServisera.length ? p.napomeneServisera[0].opis : <span className="text-muted">-</span>}</div>
                        <div className="mt-1"><strong>Zamjensko vozilo:</strong> {p.rezervacijaZamjene ? `${p.rezervacijaZamjene.registracija} (${p.rezervacijaZamjene.datumOd} â€” ${p.rezervacijaZamjene.datumDo})` : <span className="text-muted">-</span>}</div>
                        {p.kvarovi && p.kvarovi.length > 0 && (
                          <div className="mt-2">
                            <strong>Kvarovi:</strong>
                            <ul className="mb-0 ms-3">
                              {p.kvarovi.map(kv => (
                                <li key={kv.id_kvar}>{kv.naziv} {kv.opis && `- ${kv.opis}`}</li>
                              ))}
                            </ul>
                          </div>
                        )}
                      </div>
                      <div className="d-flex gap-2 flex-wrap">
                        {/* Serviser can postpone term, change status, add note */}
                        <button className="btn btn-sm btn-outline-secondary" onClick={()=>openEditTermin(p)}>Odgodi termin</button>
                        <button className="btn btn-sm btn-outline-primary" onClick={()=>setStatusModal({ idPrijava: p.idPrijava, noviStatus: p.status })}>Promijeni status</button>
                        <button className="btn btn-sm btn-outline-secondary" onClick={()=>setNoteModal({ idPrijava: p.idPrijava, opis: '' })}>Dodaj napomenu</button>
                        <button className="btn btn-sm btn-outline-danger" onClick={()=>handleCompletePrijava(p.idPrijava)}>ZavrĹˇi servis</button>
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
                          alert('Seed zavrĹˇen. Provjeri dropdown.');
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
                        <label className="form-label">Odaberite servisera</label>
                      <select className="form-select" value={selectedServiser} onChange={e=>setSelectedServiser(e.target.value)} required>
                        <option value="">-- odaberite --</option>
                        {serviseri.map(s => <option key={s.idServiser} value={s.idServiser}>{s.imePrezime}</option>)}
                      </select>
                    </div>
                  <div className="mb-3">
                    <label className="form-label">Odaberite dan</label>
                    <DatePicker
                      selected={selectedDatum}
                      onChange={(date) => {
                        if (!date) {
                          setSelectedDatum(null);
                          setSelectedTermin("");
                          return;
                        }
                        const dateStr = toDateString(date);
                        if (!availableDates.includes(dateStr)) {
                          setSelectedDatum(null);
                          setSelectedTermin("");
                          setError("Za odabrani dan nema dostupnih termina.");
                          return;
                        }
                        setError("");
                        setSelectedDatum(date);
                        setSelectedTermin("");
                      }}
                      className="form-control"
                      placeholderText="Odaberite dan"
                      includeDates={availableDateObjects}
                      minDate={minDateObj}
                      maxDate={maxDateObj}
                      disabled={availableDates.length === 0}
                      dateFormat="dd.MM.yyyy"
                    />
                    {selectedDatum && !availableDates.includes(toDateString(selectedDatum)) && (
                      <div className="text-danger small mt-1">Za odabrani dan nema termina.</div>
                    )}
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Odaberite termin</label>
                    {selectedDatum ? (
                      <div className="border rounded p-2 bg-light">
                        <div className="d-flex flex-wrap gap-2">
                          {timeSlotsForDate(toDateString(selectedDatum)).map(t => (
                            <button
                              key={t.idTermin}
                              type="button"
                              className={`btn btn-sm ${String(selectedTermin) === String(t.idTermin) ? "btn-primary" : "btn-outline-primary"}`}
                              onClick={() => setSelectedTermin(String(t.idTermin))}
                            >
                              {new Date(t.datumVrijeme).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                            </button>
                          ))}
                          {timeSlotsForDate(toDateString(selectedDatum)).length === 0 && (
                            <div className="text-muted">Nema termina za odabrani dan.</div>
                          )}
                        </div>
                      </div>
                    ) : (
                      <div className="text-muted">Prvo odaberite dan u kalendaru.</div>
                    )}
                  </div>

                    <div className="mb-3">
                      <label className="form-label">Odaberite kvarove (defekte)</label>
                      <div className="border rounded p-2">
                        {kvarovi.length === 0 ? (
                          <div className="text-muted">Nema dostupnih kvarova</div>
                        ) : (
                          kvarovi.map(kvar => (
                            <div key={kvar.id_kvar} className="form-check">
                              <input
                                className="form-check-input"
                                type="checkbox"
                                id={`kvar-${kvar.id_kvar}-admin`}
                                checked={selectedKvarovi.includes(kvar.id_kvar)}
                                onChange={e => {
                                  if (e.target.checked) {
                                    setSelectedKvarovi([...selectedKvarovi, kvar.id_kvar]);
                                  } else {
                                    setSelectedKvarovi(selectedKvarovi.filter(k => k !== kvar.id_kvar));
                                  }
                                }}
                              />
                              <label className="form-check-label" htmlFor={`kvar-${kvar.id_kvar}-admin`}>
                                <strong>{kvar.naziv}</strong> {kvar.opis && `- ${kvar.opis}`}
                              </label>
                            </div>
                          ))
                        )}
                      </div>
                    </div>

                    <div className="mb-3">
                      <label className="form-label">Napomena</label>
                      <textarea className="form-control" value={napomena} onChange={e=>setNapomena(e.target.value)} rows={3}></textarea>
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? "Slanje..." : "PoĹˇalji prijavu"}</button>
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
                          <div><strong>Status:</strong> {renderStatus(p.status)}</div>
                          <div><strong>Termin:</strong> {p.datumTermina ? new Date(p.datumTermina).toLocaleString() : '-'}</div>
                          <div><strong>Vozilo:</strong> {p.voziloInfo}</div>
                          <div><strong>Serviser:</strong> {p.serviserIme}</div>
                          <div className="mt-2"><strong>Napomena vlasnika:</strong> {p.napomenaVlasnika || <span className="text-muted">-</span>}</div>

                          {p.napomeneServisera && p.napomeneServisera.length > 0 && (
                            <div className="mt-2">
                              <strong>Napomene servisera:</strong>
                              <ul className="list-unstyled small mb-0">
                                {p.napomeneServisera.map(n => (
                                  <li key={n.idNapomena}><em>{new Date(n.datum).toLocaleString()}:</em> {n.opis}</li>
                                ))}
                              </ul>
                            </div>
                          )}
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
                                  alert('GreĹˇka pri brisanju prijave');
                                }
                              }}>ObriĹˇi</button>
                              {!p.rezervacijaZamjene && (user.idOsoba === p.idVlasnik || user.uloga === 'administrator') && (
                                <button className="btn btn-sm btn-outline-secondary" onClick={()=>openZamjena(p)}>Rezerviraj zamjensko vozilo</button>
                              )}
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
                          <div><strong>Status:</strong> {renderStatus(p.status)}</div>
                          {p.kvarovi && p.kvarovi.length > 0 && (
                            <div className="mt-2">
                              <strong>Kvarovi:</strong>
                              <ul className="mb-0 ms-3">
                                {p.kvarovi.map(kv => (
                                  <li key={kv.id_kvar}>{kv.naziv} {kv.opis && `- ${kv.opis}`}</li>
                                ))}
                              </ul>
                            </div>
                          )}
                        </div>
                        <div className="d-flex gap-2 flex-wrap">
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
                <h5 className="modal-title">Odgodi termin</h5>
                <button type="button" className="btn-close" onClick={()=>setEditTerminModal(null)} aria-label="Close"></button>
              </div>
              <div className="modal-body">
                {error && <div className="alert alert-danger">{error}</div>}
                <div className="mb-3">
                  <label className="form-label">Odaberite novi termin</label>
                  {editTerminModal.available && editTerminModal.available.length > 0 ? (
                    <select className="form-select" value={editTerminModal.selectedSlot || ''} onChange={e=>{
                      setEditTerminModal({...editTerminModal, selectedSlot: e.target.value});
                    }}>
                      <option value="">-- odaberite slobodan termin --</option>
                      {editTerminModal.available.map(t => <option key={t.idTermin} value={t.datumVrijeme}>{new Date(t.datumVrijeme).toLocaleString()}</option>)}
                    </select>
                  ) : (
                    <div className="alert alert-info">Nema dostupnih termina za odgodu.</div>
                  )}
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
                  <input type="date" className="form-control" value={zamjenaModal.datumOd} min={zamjenaModal.minDate || ""} onChange={e => setZamjenaModal({...zamjenaModal, datumOd: e.target.value})} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Datum do</label>
                  <input type="date" className="form-control" value={zamjenaModal.datumDo} min={(zamjenaModal.datumOd || zamjenaModal.minDate) || ""} onChange={e => setZamjenaModal({...zamjenaModal, datumDo: e.target.value})} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Dostupna vozila</label>
                  <select className="form-select" value={zamjenaModal.selectedZamjena || ''} onChange={e => setZamjenaModal({...zamjenaModal, selectedZamjena: e.target.value})}>
                    <option value="">-- odaberite zamjensko vozilo --</option>
                    {(zamjenaModal.available || []).map(z => <option key={z.id_zamjena ?? z.idZamjena} value={z.id_zamjena ?? z.idZamjena}>{formatZamjenaLabel(z)}</option>)}
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

      {/* Status modal for serviser */}
      {statusModal && (
        <div className="modal show d-block" tabIndex={-1} role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Promijeni status prijave</h5>
                <button type="button" className="btn-close" onClick={()=>setStatusModal(null)} aria-label="Close"></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">Novi status</label>
                  <select className="form-select" value={statusModal.noviStatus} onChange={e=>setStatusModal({...statusModal, noviStatus: e.target.value})}>
                    <option value="zaprimljeno">zaprimljeno</option>
                    <option value="u obradi">u obradi</option>
                    <option value="odgoÄ‘eno">odgoÄ‘eno</option>
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={()=>setStatusModal(null)}>Odustani</button>
                <button type="button" className="btn btn-primary" onClick={handleStatusSave}>Spremi</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Note modal for serviser */}
      {noteModal && (
        <div className="modal show d-block" tabIndex={-1} role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Dodaj napomenu</h5>
                <button type="button" className="btn-close" onClick={()=>setNoteModal(null)} aria-label="Close"></button>
              </div>
              <div className="modal-body">
                <div className="mb-3">
                  <label className="form-label">Opis napomene</label>
                  <textarea className="form-control" value={noteModal.opis} onChange={e=>setNoteModal({...noteModal, opis: e.target.value})} rows={4}></textarea>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={()=>setNoteModal(null)}>Odustani</button>
                <button type="button" className="btn btn-primary" onClick={handleAddNote}>Spremi</button>
              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}

export default Appointments;






