import { useEffect, useState } from "react";
import axios from "axios";
import 'bootstrap/dist/css/bootstrap.min.css';

function Zamjene({ user }) {
  const [zamjene, setZamjene] = useState([]);
  const [marke, setMarke] = useState([]);
  const [modeli, setModeli] = useState([]);
  const [novo, setNovo] = useState({ id_marka: "", id_model: "", registracija: "", dostupno: true });

  useEffect(() => {
    if (!user || user.uloga !== 'administrator') return;
    axios.get('/api/zamjene/all')
      .then(r => setZamjene(r.data))
      .catch(e => {
        console.error(e);
        const data = e?.response?.data;
        const msg = typeof data === 'string' ? data : (data?.message || (typeof data === 'object' ? JSON.stringify(data) : null));
        alert(msg || 'Greška pri dohvaćanju zamjena (provjerite konzolu)');
      });
  }, [user]);

  useEffect(() => {
    axios.get('/api/marke').then(r => setMarke(r.data)).catch(e => console.error(e));
  }, []);

  useEffect(() => {
    if (novo.id_marka) {
      axios.get(`/api/modeli/${novo.id_marka}`).then(r => setModeli(r.data)).catch(e => console.error(e));
    } else setModeli([]);
  }, [novo.id_marka]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setNovo({ ...novo, [name]: type === 'checkbox' ? checked : value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('/api/zamjene', {
        idModel: Number(novo.id_model),
        registracija: novo.registracija,
        dostupno: novo.dostupno
      });
      const r = await axios.get('/api/zamjene/all');
      setZamjene(r.data);
      setNovo({ id_marka: "", id_model: "", registracija: "", dostupno: true });
    } catch (err) {
      console.error(err);
      const data = err?.response?.data;
      const msg = typeof data === 'string' ? data : (data?.message || (typeof data === 'object' ? JSON.stringify(data) : err?.message));
      alert(msg || 'Greška pri dodavanju zamjenskog vozila');
    }
  };

  if (!user) return (<div className="container mt-5 text-center"><h4>Molimo prijavite se.</h4></div>);
  if (user && user.uloga !== 'administrator') return (<div className="container mt-5 text-center"><h4>Samo administrator može upravljati zamjenskim vozilima.</h4></div>);

  return (
    <div className="container mt-4">
      <h2 className="mb-3">Zamjenska vozila</h2>

      <div className="card p-4 mb-4 shadow-sm">
        <h5 className="mb-3">Dodaj zamjensko vozilo</h5>
        <form onSubmit={handleSubmit}>
          <div className="row mb-3">
            <div className="col-md-4">
              <label className="form-label">Marka</label>
              <select name="id_marka" className="form-select" value={novo.id_marka} onChange={handleChange} required>
                <option value="">Odaberi marku...</option>
                {marke.map(m => <option key={m.idMarka} value={m.idMarka}>{m.naziv}</option>)}
              </select>
            </div>

            <div className="col-md-4">
              <label className="form-label">Model</label>
              <select name="id_model" className="form-select" value={novo.id_model} onChange={handleChange} required disabled={!novo.id_marka}>
                <option value="">Odaberi model...</option>
                {modeli.map(mod => <option key={mod.idModel} value={mod.idModel}>{mod.naziv}</option>)}
              </select>
            </div>

            <div className="col-md-4">
              <label className="form-label">Registracija</label>
              <input type="text" name="registracija" className="form-control" value={novo.registracija} onChange={handleChange} required />
            </div>
          </div>

          <div className="form-check mb-3">
            <input className="form-check-input" name="dostupno" type="checkbox" checked={novo.dostupno} onChange={handleChange} id="dostupnoCheck" />
            <label className="form-check-label" htmlFor="dostupnoCheck">Dostupno</label>
          </div>

          <button type="submit" className="btn btn-success">Dodaj</button>
        </form>
      </div>

      {zamjene.length > 0 ? (
        <table className="table table-striped mt-3">
          <thead>
            <tr>
              <th>Registracija</th>
              <th>Model</th>
              <th>Dostupno</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {zamjene.map(z => (
              <tr key={z.id_zamjena}>
                <td>{z.registracija}</td>
                <td>{z.model?.naziv || ''}</td>
                <td>{z.dostupno ? 'Da' : 'Ne'}</td>
                <td>
                  <button className="btn btn-sm btn-danger" onClick={async ()=>{
                    if (!window.confirm('Obrisati ovo zamjensko vozilo?')) return;
                    try {
                      await axios.delete(`/api/zamjene/${z.id_zamjena}`);
                      const r = await axios.get('/api/zamjene/all');
                      setZamjene(r.data);
                    } catch (err) { console.error(err); alert('Greška pri brisanju'); }
                  }}>Obriši</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (<p>Nema zamjenskih vozila.</p>)}

    </div>
  );
}

export default Zamjene;
