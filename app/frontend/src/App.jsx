import { useEffect, useState } from "react";
import axios from "axios";
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Vozila from './pages/Vozila';
import Kontakt from './pages/Kontakt';

axios.defaults.baseURL = process.env.REACT_APP_API_URL;
axios.defaults.withCredentials = true;                    // šalje cookies

const API = process.env.REACT_APP_API_URL;

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    axios.get("/api/auth/user")
        .then((res) => setUser(res.data))
        .catch(() => setUser(null));
  }, []);


  return (
    <Router>
      <UseAuthRefresh>
        <Navbar user={user} />
        <div className="container mt-4">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/vozila" element={<Vozila user={user} />} />
            <Route path="/kontakt" element={<Kontakt />} />
          </Routes>
        </div>
      </UseAuthRefresh>
    </Router>
  );
}

function UseAuthRefresh({ children }) {
  const location = useLocation();

  useEffect(() => {
    const ok = new URLSearchParams(location.search).get("login") === "success";
    if (ok) {
      axios.get("/api/auth/user")
           .then(r => console.log("Refetched user after login success"))
           .catch(console.error);
    }
  }, [location.search]);

  return children;
}

export default App;