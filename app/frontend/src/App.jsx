import { useEffect, useState } from "react";
import axios from "axios";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  useLocation,
  useNavigate,
} from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Vozila from "./pages/Vozila";
import Kontakt from "./pages/Kontakt";

// --- AXIOS GLOBAL ---
axios.defaults.baseURL = process.env.REACT_APP_API_URL;
axios.defaults.withCredentials = false; // koristimo JWT u headeru, ne cookies

const TOKEN_KEY = "auth_token";

function setAuthHeader(token) {
  if (token) {
    axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  } else {
    delete axios.defaults.headers.common["Authorization"];
  }
}

function readTokenFromURL(location) {
  // Podržimo i query (?token=...) i hash (#token=...) za svaki slučaj
  const q = new URLSearchParams(location.search);
  const fromQuery = q.get("token");

  let fromHash = null;
  if (location.hash && location.hash.startsWith("#")) {
    const h = new URLSearchParams(location.hash.slice(1));
    fromHash = h.get("token");
  }

  return fromQuery || fromHash;
}

function AppRoutes() {
  const [user, setUser] = useState(null);
  const location = useLocation();
  const navigate = useNavigate();

  // 0) na mount: ako postoji token u storageu -> stavi header i pokušaj dohvatiti usera
  useEffect(() => {
    const saved = sessionStorage.getItem(TOKEN_KEY);
    if (saved) {
      setAuthHeader(saved);
      axios
        .get("/api/auth/user")
        .then((res) => setUser(res.data))
        .catch(() => setUser(null));
    }
  }, []);

  // 1) obradi povratak s OAuth-a: pokupi token iz URL-a, spremi, postavi header, refetch usera, očisti URL
  useEffect(() => {
    const q = new URLSearchParams(location.search);
    const loginStatus = q.get("login");
    const token = readTokenFromURL(location);

    if (loginStatus === "success" && token) {
      sessionStorage.setItem(TOKEN_KEY, token);
      setAuthHeader(token);

      axios
        .get("/api/auth/user")
        .then((res) => setUser(res.data))
        .catch(() => setUser(null))
        .finally(() => {
          // makni ?login=success i token iz URL-a bez reloada
          navigate("/", { replace: true });
        });
    } else if (loginStatus === "fail") {
      // po želji, možeš prikazati toast/alert
      navigate("/", { replace: true });
    }
  }, [location.search, location.hash, navigate]);

  // 2) fallback: ako još nemamo usera (npr. prvi ulazak bez tokena), pokušaj dohvatiti (nije obavezno)
  useEffect(() => {
    if (user == null) {
      axios
        .get("/api/auth/user")
        .then((res) => setUser(res.data))
        .catch(() => setUser(null));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      <Navbar user={user} />
      <div className="container mt-4">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/vozila" element={<Vozila user={user} />} />
          <Route path="/kontakt" element={<Kontakt />} />
        </Routes>
      </div>
    </>
  );
}

export default function App() {
  return (
    <Router>
      <AppRoutes />
    </Router>
  );
}