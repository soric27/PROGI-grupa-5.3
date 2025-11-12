import { useEffect, useState } from "react";
import axios from "axios";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  useLocation,
} from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Vozila from "./pages/Vozila";
import Kontakt from "./pages/Kontakt";

axios.defaults.baseURL = process.env.REACT_APP_API_URL;
axios.defaults.withCredentials = true;

function AppRoutes() {
  const [user, setUser] = useState(null);
  const location = useLocation();

  // početno učitavanje korisnika (ako je već logiran)
  useEffect(() => {
    axios
      .get("/api/auth/user")
      .then((res) => setUser(res.data))
      .catch(() => setUser(null));
  }, []);

  // nakon povratka s Google-a
  useEffect(() => {
    const q = new URLSearchParams(location.search);
    if (q.get("login") !== "success") return;

    let cancelled = false;

    const tryFetch = async () => {
      const maxTries = 6;           // ~ do 6 pokušaja
      const delayMs = [300, 600, 1000, 1500, 2000, 3000]; // progresivno
      for (let i = 0; i < maxTries && !cancelled; i++) {
        try {
          const res = await axios.get("/api/auth/user");
          if (!cancelled) {
            setUser(res.data);
          }
          return; // uspjelo
        } catch (e) {
          // pričekaj i pokušaj opet
          await new Promise((r) => setTimeout(r, delayMs[i]));
        }
      }
    };

    tryFetch();
    return () => {
      cancelled = true;
    };
  }, [location.search]);

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