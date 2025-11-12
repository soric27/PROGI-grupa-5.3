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

  // ucitaj trenutnog korisnika
  useEffect(() => {
    axios
      .get("/api/auth/user")
      .then((res) => setUser(res.data))
      .catch(() => setUser(null));
  }, []);

  // nakon povratka s Google login-a ponovo dohvatiti korisnika
  useEffect(() => {
    const q = new URLSearchParams(location.search);
    if (q.get("login") === "success") {
      axios
        .get("/api/auth/user")
        .then((res) => setUser(res.data))
        .catch(() => setUser(null));
    }
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