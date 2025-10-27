import { useEffect, useState } from "react";
import axios from "axios";
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Vozila from './pages/Vozila';
import Kontakt from './pages/Kontakt';

function App() {
  const [user, setUser] = useState(null);

  useEffect(() => {
    axios
      .get("http://localhost:5000/auth/user", { withCredentials: true })
      .then((res) => setUser(res.data))
      .catch(() => setUser(null));
  }, []);

  return (
    <Router>
      <Navbar user={user} />
      <div className="container mt-4">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/vozila" element={<Vozila user={user} />} />
          <Route path="/kontakt" element={<Kontakt />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;