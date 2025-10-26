import { useState } from 'react';
import { Link } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import logo from '../assets/auto-servis-logo.png';

function Navbar() {
  const [showLogin, setShowLogin] = useState(false);

  const handleToggleLogin = () => {
    setShowLogin(!showLogin);
  };

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
        <div className="container">
          <Link className="navbar-brand d-flex align-items-center" to="/">
            <img src={logo} alt="Auto Servis Logo" height="60" className="me-2" />
            Auto Servis
          </Link>
          <div className="collapse navbar-collapse">
            <ul className="navbar-nav ms-auto">
              <li className="nav-item">
                <Link className="nav-link" to="/">Početna</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/vozila">Vozila</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/kontakt">Kontakt</Link>
              </li>
              <li className="nav-item">
                <button onClick={handleToggleLogin} className="btn btn-outline-light ms-3">
                  {showLogin ? 'Zatvori' : 'Login'}
                </button>
              </li>
            </ul>
          </div>
        </div>
      </nav>

      {showLogin && (
  <div className="login-overlay bg-secondary bg-opacity-25 py-5">
    <div className="login-box bg-light p-4 text-dark rounded shadow">
      <h5 className="text-center mb-3">Prijava korisnika</h5>
      <form>
        <div className="mb-3">
          <label className="form-label">Korisničko ime</label>
          <input type="text" className="form-control" placeholder="Unesite korisničko ime" />
        </div>
        <div className="mb-3">
          <label className="form-label">Lozinka</label>
          <input type="password" className="form-control" placeholder="Unesite lozinku" />
        </div>
        <div className="text-end">
          <button type="submit" className="btn btn-primary">Prijavi se</button>
        </div>
      </form>
    </div>
  </div>
)}

    </>
  );
}

export default Navbar;
