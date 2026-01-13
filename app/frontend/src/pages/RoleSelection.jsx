import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import "bootstrap/dist/css/bootstrap.min.css";

function RoleSelection() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const tokenFromQuery = searchParams.get("token");
  const token = tokenFromQuery || sessionStorage.getItem("auth_token");

  // Ako nema tokena, preusmjeri na home
  useEffect(() => {
    if (!token) {
      navigate("/");
    }
  }, [token, navigate]);

  const handleSelectRole = async (role) => {
    setLoading(true);
    setError("");

    try {
      // Postavi Authorization header sa tokenom
      axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;

      // Pošalji izbor uloge
      const response = await axios.post("/api/auth/select-role", {
        uloga: role,
      });

      // Spremi novi token
      sessionStorage.setItem("auth_token", response.data.token);
      axios.defaults.headers.common["Authorization"] = `Bearer ${response.data.token}`;

      // Preusmjeri na home (full reload so App sees updated token)
      window.location.replace("/");
    } catch (err) {
      console.error("Greška pri izboru uloge:", err);
      setError("Greška pri izboru uloge. Pokušajte ponovno.");
      setLoading(false);
    }
  };

  if (!token) {
    return null;
  }

  return (
    <div className="d-flex align-items-center justify-content-center" style={{ minHeight: "100vh" }}>
      <div className="card p-5 shadow" style={{ width: "400px" }}>
        <h2 className="text-center mb-4">Odaberite vašu ulogu</h2>

        {error && <div className="alert alert-danger">{error}</div>}

        <div className="d-grid gap-3">
          <button
            className="btn btn-primary btn-lg"
            onClick={() => handleSelectRole("korisnik")}
            disabled={loading}
          >
            {loading ? "Učitavanje..." : "👤 Obični korisnik"}
          </button>

          <button
            className="btn btn-info btn-lg"
            onClick={() => handleSelectRole("serviser")}
            disabled={loading}
          >
            {loading ? "Učitavanje..." : "🔧 Serviser"}
          </button>

          <button
            className="btn btn-warning btn-lg"
            onClick={() => handleSelectRole("administrator")}
            disabled={loading}
          >
            {loading ? "Učitavanje..." : "👨‍💼 Administrator"}
          </button>
        </div>

        <p className="text-center text-muted mt-4 small">
          Ova ulogu možete promijeniti kasnije u postavkama.
        </p>
      </div>
    </div>
  );
}

export default RoleSelection;
