import React, { Component } from "react";
import ApiService from "../services/ApiService";
import "bootstrap/dist/css/bootstrap.min.css";

class Vozila extends Component {
  constructor(props) {
    super(props);
    this.state = {
      vozila: [],
      marke: [],
      modeli: [],
      novoVozilo: {
        id_marka: "",
        id_model: "",
        registracija: "",
        godina_proizvodnje: "",
      },
      showForm: false,
    };
  }

  componentDidMount() {
    this.loadMarke();
    if (this.props.user) {
      this.loadVozila();
    }
  }

  componentDidUpdate(prevProps) {
    if (this.props.user && !prevProps.user) {
      this.loadVozila();
    }
  }

  async loadVozila() {
    try {
      const vozila = await ApiService.getVozila();
      this.setState({ vozila });
    } catch (error) {
      console.error("Greška pri dohvaćanju vozila:", error);
    }
  }

  async loadMarke() {
    try {
      const marke = await ApiService.getMarke();
      this.setState({ marke });
    } catch (error) {
      console.error("Greška pri dohvaćanju marki:", error);
    }
  }

  async loadModeli(id_marka) {
    try {
      const modeli = await ApiService.getModeliByMarka(id_marka);
      this.setState({ modeli });
    } catch (error) {
      console.error("Greška pri dohvaćanju modela:", error);
    }
  }

  handleChange = (e) => {
    const { name, value } = e.target;
    this.setState(
      (prevState) => ({
        novoVozilo: {
          ...prevState.novoVozilo,
          [name]: value,
        },
      }),
      () => {
        if (name === "id_marka" && value) {
          this.loadModeli(value);
        }
      }
    );
  };

  handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const { novoVozilo } = this.state;
      await ApiService.addVozilo({
        id_model: Number(novoVozilo.id_model),
        registracija: novoVozilo.registracija,
        godina_proizvodnje: Number(novoVozilo.godina_proizvodnje),
      });

      await this.loadVozila();

      this.setState({
        novoVozilo: {
          id_marka: "",
          id_model: "",
          registracija: "",
          godina_proizvodnje: "",
        },
        showForm: false,
      });
    } catch (error) {
      console.error("Greška pri dodavanju vozila:", error);
    }
  };

  toggleForm = () => {
    this.setState((prevState) => ({ showForm: !prevState.showForm }));
  };

  render() {
    const { user } = this.props;
    const { vozila, marke, modeli, novoVozilo, showForm } = this.state;

    if (!user) {
      return (
        <div className="container mt-5">
          <p className="text-center">Molimo prijavite se da biste vidjeli svoja vozila.</p>
        </div>
      );
    }

    return (
      <div className="container mt-5">
        <h2>Moja Vozila</h2>
        <button className="btn btn-primary mb-3" onClick={this.toggleForm}>
          {showForm ? "Zatvori formu" : "+ Dodaj vozilo"}
        </button>

        {showForm && (
          <form onSubmit={this.handleSubmit} className="mb-4 p-3 border rounded">
            <div className="mb-3">
              <label className="form-label">Marka</label>
              <select
                name="id_marka"
                className="form-select"
                value={novoVozilo.id_marka}
                onChange={this.handleChange}
                required
              >
                <option value="">Odaberi marku</option>
                {marke.map((m) => (
                  <option key={m.id_marka} value={m.id_marka}>
                    {m.naziv}
                  </option>
                ))}
              </select>
            </div>

            <div className="mb-3">
              <label className="form-label">Model</label>
              <select
                name="id_model"
                className="form-select"
                value={novoVozilo.id_model}
                onChange={this.handleChange}
                required
                disabled={!novoVozilo.id_marka}
              >
                <option value="">Odaberi model</option>
                {modeli.map((m) => (
                  <option key={m.id_model} value={m.id_model}>
                    {m.naziv}
                  </option>
                ))}
              </select>
            </div>

            <div className="mb-3">
              <label className="form-label">Registracija</label>
              <input
                type="text"
                name="registracija"
                className="form-control"
                value={novoVozilo.registracija}
                onChange={this.handleChange}
                required
              />
            </div>

            <div className="mb-3">
              <label className="form-label">Godina proizvodnje</label>
              <input
                type="number"
                name="godina_proizvodnje"
                className="form-control"
                value={novoVozilo.godina_proizvodnje}
                onChange={this.handleChange}
                required
              />
            </div>

            <button type="submit" className="btn btn-success">
              Spremi
            </button>
          </form>
        )}

        {vozila.length > 0 ? (
          <table className="table table-striped">
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
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p className="text-muted">Još niste prijavili nijedno vozilo.</p>
        )}
      </div>
    );
  }
}

export default Vozila;
