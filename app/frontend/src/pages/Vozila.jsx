import { useEffect, useState } from "react";
import axios from "axios";
import "bootstrap/dist/css/bootstrap.min.css";

function Vozila({ user }) {
  const [vozila, setVozila] = useState([]);
  const [users, setUsers] = useState([]);
  const [selectedUserForAdmin, setSelectedUserForAdmin] = useState("");
  const [marke, setMarke] = useState([]);
  const [modeli, setModeli] = useState([]);
  const [novoVozilo, setNovoVozilo] = useState({
    id_marka: "",
    id_model: "",
    registracija: "",
    godina_proizvodnje: "",
  });
  const [showForm, setShowForm] = useState(false);

  // vozila prijavljenog korisnika
  useEffect(() => {
    if (user) {
      if (user.uloga === "administrator" && selectedUserForAdmin) {
        axios
          .get(`/api/vehicles/for/${selectedUserForAdmin}`, { withCredentials: true })
          .then((res) => setVozila(res.data))
          .catch((err) => console.error("Greška pri dohvaæanju vozila:", err));
      } else {
        axios
          .get("/api/vehicles", { withCredentials: true })
          .then((res) => setVozila(res.data))
          .catch((err) => console.error("Greška pri dohvaæanju vozila:", err));
      }
    }
  }, [user, selectedUserForAdmin]);

  // marke automobila
  useEffect(() => {
    axios
      .get("/api/marke")
      .then((res) => setMarke(res.data))
      .catch((err) => console.error("Greška pri dohvaæanju marki:", err));

    if (user && user.uloga === "administrator") {
      axios
        .get("/api/users")
        .then((r) => setUsers(r.data))
        .catch((err) => console.error("Greška pri dohvatu korisnika", err));
    }
  }, [user]);

  // modeli odredjene marke
  useEffect(() => {
    if (novoVozilo.id_marka) {
      axios
        .get(`/api/modeli/${novoVozilo.id_marka}`)
        .then((res) => setModeli(res.data))
        .catch((err) => console.error("Greška pri dohvaæanju modela:", err));
    } else {
      setModeli([]);
    }
  }, [novoVozilo.id_marka]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setNovoVozilo({ ...novoVozilo, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      if (user && user.uloga === "administrator" && selectedUserForAdmin) {
        await axios.post(
          `/api/vehicles/for/${selectedUserForAdmin}`,
          {
            id_model: Number(novoVozilo.id_model),
            registracija: novoVozilo.registracija,
            godina_proizvodnje: Number(novoVozilo.godina_proizvodnje),
          },
          { withCredentials: true }
        );

        const response = await axios.get(`/api/vehicles/for/${selectedUserForAdmin}`, {
          withCredentials: true,
        });
        setVozila(response.data);
      } else {
        await axios.post(
          "/api/vehicles",
          {
            id_model: Number(novoVozilo.id_model),
            registracija: novoVozilo.registracija,
            godina_proizvodnje: Number(novoVozilo.godina_proizvodnje),
          },
          { withCredentials: true }
        );

        const response = await axios.get("/api/vehicles", {
          withCredentials: true,
        });
        setVozila(response.data);
      }

      setNovoVozilo({
        id_marka: "",
        id_model: "",
        registracija: "",
        godina_proizvodnje: "",
      });
      setShowForm(false);
    } catch (err) {
      console.error("Greška pri dodavanju vozila:", err);
    }
  };

  if (!user) {
    return (
      <div className="container mt-5 text-center">
        <h4>Molimo prijavite se kako biste vidjeli i prijavili svoja vozila.</h4>
      </div>
    );
  }

  return (
    <div className="container mt-4">
      <h2 className="mb-3">Moja vozila</h2>

      {(user.uloga === "korisnik" || user.uloga === "administrator") && (
        <button className="btn btn-primary mb-3" onClick={() => setShowForm(!showForm)}>
          {showForm ? "Zatvori unos" : "Dodaj vozilo"}
        </button>
      )}

      {user.uloga === "administrator" && (
        <div className="mb-3">
          <label className="form-label">Odaberite korisnika</label>
          <select
            className="form-select"
            value={selectedUserForAdmin}
            onChange={(e) => setSelectedUserForAdmin(e.target.value)}
          >
            <option value="">-- odaberite korisnika --</option>
            {users.map((u) => (
              <option key={u.idOsoba} value={u.idOsoba}>
                {u.imePrezime} ({u.email})
              </option>
            ))}
          </select>
        </div>
      )}

      {showForm && (user.uloga === "korisnik" || user.uloga === "administrator") && (
        <div className="card p-4 mb-4 shadow-sm">
          <h5 className="mb-3">Novi unos vozila</h5>
          <form onSubmit={handleSubmit}>
            <div className="row mb-3">
              <div className="col-md-4">
                <label className="form-label">Marka</label>
                <select
                  name="id_marka"
                  className="form-select"
                  value={novoVozilo.id_marka}
                  onChange={handleChange}
                  required
                >
                  <option value="">Odaberi marku...</option>
                  {marke.map((m) => (
                    <option key={m.idMarka} value={m.idMarka}>
                      {m.naziv}
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-4">
                <label className="form-label">Model</label>
                <select
                  name="id_model"
                  className="form-select"
                  value={novoVozilo.id_model}
                  onChange={handleChange}
                  required
                  disabled={!novoVozilo.id_marka}
                >
                  <option value="">Odaberi model...</option>
                  {modeli.map((mod) => (
                    <option key={mod.idModel} value={mod.idModel}>
                      {mod.naziv}
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-4">
                <label className="form-label">Godina proizvodnje</label>
                <input
                  type="number"
                  name="godina_proizvodnje"
                  className="form-control"
                  value={novoVozilo.godina_proizvodnje}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="mb-3">
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

            <button type="submit" className="btn btn-success">
              Spremi vozilo
            </button>
          </form>
        </div>
      )}

      {vozila.length > 0 ? (
        <table className="table table-striped mt-3">
          <thead>
            <tr>
              <th>Marka</th>
              <th>Model</th>
              <th>Registracija</th>
              <th>Godina</th>
            </tr>
          </thead>
          <tbody>
            {vozila.map((v) => (
              <tr key={v.id_vozilo}>
                <td>{v.marka_naziv}</td>
                <td>{v.model_naziv}</td>
                <td>{v.registracija}</td>
                <td>{v.godina_proizvodnje}</td>
                {user && (
                  <td>
                    <button
                      className="btn btn-sm btn-danger"
                      onClick={async () => {
                        if (!window.confirm("Obrisati ovo vozilo?")) return;
                        try {
                          await axios.delete(`/api/vehicles/${v.id_vozilo}`);
                          if (selectedUserForAdmin) {
                            const r = await axios.get(`/api/vehicles/for/${selectedUserForAdmin}`);
                            setVozila(r.data);
                          } else {
                            const r = await axios.get("/api/vehicles");
                            setVozila(r.data);
                          }
                        } catch (err) {
                          console.error(err);
                          alert("Greška pri brisanju vozila");
                        }
                      }}
                    >
                      Obriši
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>Još niste prijavili nijedno vozilo.</p>
      )}
    </div>
  );
}

export default Vozila;
