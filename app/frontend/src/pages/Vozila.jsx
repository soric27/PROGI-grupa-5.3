import { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';

function Vozila() {
  const [vozila, setVozila] = useState([]);
  const [novoVozilo, setNovoVozilo] = useState({
    model: '',
    registracija: '',
    serviser: '',
    datum: '',
    zamjensko: false,
  });
  const [showForm, setShowForm] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setNovoVozilo({
      ...novoVozilo,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setVozila([...vozila, novoVozilo]);
    setNovoVozilo({ model: '', registracija: '', serviser: '', datum: '', zamjensko: false });
    setShowForm(false);
  };

  return (
    <div className="container mt-4">
      <h2 className="mb-3">Pregled prijavljenih vozila</h2>

      <button
        className="btn btn-primary mb-3"
        onClick={() => setShowForm(!showForm)}
      >
        {showForm ? 'Zatvori unos' : 'Dodaj vozilo'}
      </button>

      {showForm && (
        <div className="card p-4 mb-4 shadow-sm">
          <h5 className="mb-3">Novi unos vozila</h5>
          <form onSubmit={handleSubmit}>
            <div className="row mb-3">
              <div className="col-md-6">
                <label className="form-label">Model vozila</label>
                <input
                  type="text"
                  name="model"
                  className="form-control"
                  value={novoVozilo.model}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="col-md-6">
                <label className="form-label">Registracija</label>
                <input
                  type="text"
                  name="registracija"
                  className="form-control"
                  value={novoVozilo.registracija}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="row mb-3">
              <div className="col-md-6">
                <label className="form-label">Serviser</label>
                <select
                  name="serviser"
                  className="form-select"
                  value={novoVozilo.serviser}
                  onChange={handleChange}
                  required
                >
                  <option value="">Odaberi servisera...</option>
                  <option value="Domagoj">Domagoj</option>
                  <option value="Ivan">Ivan</option>
                  <option value="Katarina">Katarina</option>
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label">Datum termina</label>
                <input
                  type="date"
                  name="datum"
                  className="form-control"
                  value={novoVozilo.datum}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="form-check mb-3">
              <input
                type="checkbox"
                name="zamjensko"
                className="form-check-input"
                checked={novoVozilo.zamjensko}
                onChange={handleChange}
              />
              <label className="form-check-label">
                Potrebno zamjensko vozilo
              </label>
            </div>

            <button type="submit" className="btn btn-success">
              Spremi vozilo
            </button>
          </form>
        </div>
      )}

      {vozila.length > 0 && (
        <table className="table table-striped mt-3">
          <thead>
            <tr>
              <th>Model</th>
              <th>Registracija</th>
              <th>Serviser</th>
              <th>Datum</th>
              <th>Zamjensko vozilo</th>
            </tr>
          </thead>
          <tbody>
            {vozila.map((v, i) => (
              <tr key={i}>
                <td>{v.model}</td>
                <td>{v.registracija}</td>
                <td>{v.serviser}</td>
                <td>{v.datum}</td>
                <td>{v.zamjensko ? 'Da' : 'Ne'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default Vozila;
