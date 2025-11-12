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

axios.defaults.baseURL = process.env.REACT_APP_API_URL;
axios.defaults.withCredentials = true;

function AppRoutes() {
  const [user, setUser] = useState(null);
  const location = useLocation();
  const navigate = useNavigate();

  // 1) učitaj korisnika na mount
  useEffect(() => {
    axios
      .get("/api/auth/user")
      .then((res) => setUser(res.data))
      .catch(() => setUser(null));
  }, []);

  // 2) nakon Google povratka (?login=success) refetch + makni query
  useEffect(() => {
    const q = new URLSearchParams(location.search);
    if (q.get("login") === "success") {
      axios
        .get("/api/auth/user")
        .then((res) => setUser(res.data))
        .finally(() => {
          // makni ?login=success iz URL-a bez reload-a
          navigate("/", { replace: true });
        });
    }
  }, [location.search, navigate]);

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