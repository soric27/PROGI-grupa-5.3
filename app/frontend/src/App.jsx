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
import RoleSelection from "./pages/RoleSelection";

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

 
  useEffect(() => {
    const saved = sessionStorage.getItem(TOKEN_KEY);
    if (saved) {
      setAuthHeader(saved);
      
      
      try {
        const payload = JSON.parse(atob(saved.split('.')[1]));
        setUser({
          email: payload.email,
          ime: payload.ime,
          prezime: payload.prezime
        });
      } catch (e) {
        setUser(null);
      }
    }
  }, []);

  
  useEffect(() => {
    const token = readTokenFromURL(location);

    if (token) {
      sessionStorage.setItem(TOKEN_KEY, token);
      setAuthHeader(token);

    
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        setUser({
          email: payload.email,
          ime: payload.ime,
          prezime: payload.prezime
        });
      } catch (e) {
        console.error("Invalid JWT", e);
        setUser(null);
      }

      // Makni token iz URL-a bez reloada
      navigate("/", { replace: true });
    }
  }, [location.search, location.hash, navigate]);

  return (
    <>
      <Navbar user={user} />
      <div className="container mt-4">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/vozila" element={<Vozila user={user} />} />
          <Route path="/kontakt" element={<Kontakt />} />
          <Route path="/role-selection" element={<RoleSelection />} />
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
