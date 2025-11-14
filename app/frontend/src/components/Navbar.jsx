import { useState } from "react";
import { Link } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import logo from "../assets/auto-servis-logo.png";

const API_URL = process.env.REACT_APP_API_URL;
const TOKEN_KEY = "auth_token";

function Navbar({ user }) {
  const [showLogin, setShowLogin] = useState(false);

  const handleToggleLogin = () => setShowLogin((v) => !v);

  // JWT logout: obriši token i refreshaj app (nije potreban poziv backendu)
  const handleLogout = (e) => {
    e.preventDefault();
    try {
      sessionStorage.removeItem(TOKEN_KEY);
    } catch {}
    // hard refresh da se sve očisti
    window.location.href = "/";
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

              <li className="nav-item ms-3">
                {user ? (
                  <>
                    <span className="text-light me-2">Pozdrav, {user.ime}</span>
                    <button onClick={handleLogout} className="btn btn-outline-light">
                      Odjava
                    </button>
                  </>
                ) : (
                  <button onClick={handleToggleLogin} className="btn btn-outline-light">
                    Login
                  </button>
                )}
              </li>
            </ul>
          </div>
        </div>
      </nav>

      {showLogin && !user && (
        <div className="login-overlay bg-secondary bg-opacity-25 py-5">
          <div className="login-box bg-light p-4 text-dark rounded shadow">
            <h5 className="text-center mb-3">Prijava korisnika</h5>
            <div className="text-center">
              <a
                href="https://autoservis-java.onrender.com/login/oauth2/code/google"
                className="btn btn-danger btn-lg"
                onClick={() => setShowLogin(false)}
              >
                <i className="bi bi-google me-2"></i> Prijava s Google računom
              </a>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default Navbar;