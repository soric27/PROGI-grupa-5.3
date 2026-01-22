import { useState } from "react";
import { Link } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import logo from "../assets/auto-servis-logo.png";

const API_URL = process.env.REACT_APP_API_URL;
const TOKEN_KEY = "auth_token";

function Navbar({ user }) {
  const [showLogin, setShowLogin] = useState(false);
  const [isNavCollapsed, setIsNavCollapsed] = useState(true); // Novo: za mobile toggle

  const handleToggleLogin = () => setShowLogin((v) => !v);
  const handleNavCollapse = () => setIsNavCollapsed(!isNavCollapsed); // Toggle funkcija

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

          {/* HAMBURGER BUTTON - prikazuje se samo na mobitelu */}
          <button 
            className="navbar-toggler" 
            type="button" 
            onClick={handleNavCollapse}
            aria-controls="navbarNav" 
            aria-expanded={!isNavCollapsed} 
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon"></span>
          </button>

          {/* Navbar sadržaj - responsive collapse */}
          <div className={`${isNavCollapsed ? 'collapse' : ''} navbar-collapse`} id="navbarNav">
            <ul className="navbar-nav ms-auto">
              {!(user && (user.uloga === 'serviser' || user.uloga === 'administrator')) && (
                <li className="nav-item">
                  <Link 
                    className="nav-link" 
                    to="/" 
                    onClick={() => setIsNavCollapsed(true)}
                  >
                    Početna
                  </Link>
                </li>
              )}

              {!(user && (user.uloga === 'serviser' || user.uloga === 'administrator')) && (
                <li className="nav-item">
                  <Link 
                    className="nav-link" 
                    to="/vozila" 
                    onClick={() => setIsNavCollapsed(true)}
                  >
                    Vozila
                  </Link>
                </li>
              )} 
              <li className="nav-item">
                <Link 
                  className="nav-link" 
                  to="/kontakt" 
                  onClick={() => setIsNavCollapsed(true)}
                >
                  Kontakt
                </Link>
              </li>

              {!(user && user.uloga === 'administrator') && (
                <li className="nav-item">
                  <Link 
                    className="nav-link" 
                    to="/appointments" 
                    onClick={() => setIsNavCollapsed(true)}
                  >
                    Termini
                  </Link>
                </li>
              )}

              {user && user.uloga === 'administrator' && (
                <li className="nav-item">
                  <Link className="nav-link" to="/servis" onClick={() => setIsNavCollapsed(true)}>Servis</Link>
                </li>
              )}

              {user && user.uloga === 'administrator' && (
                <li className="nav-item">
                  <Link className="nav-link" to="/zamjene" onClick={() => setIsNavCollapsed(true)}>Zamjenska vozila</Link>
                </li>
              )}

              {user && (user.uloga === 'serviser' || user.uloga === 'administrator') && (
                <li className="nav-item">
                  <Link className="nav-link" to="/statistika" onClick={() => setIsNavCollapsed(true)}>Statistika</Link>
                </li>
              )}

              {user && user.uloga === 'administrator' && (
                <li className="nav-item">
                  <Link className="nav-link" to="/osobe" onClick={() => setIsNavCollapsed(true)}>Osobe</Link>
                </li>
              )}

              <li className="nav-item ms-3">
                {user ? (
                  <>
                    <span className="text-light me-2">Pozdrav, {user.ime}</span>
                    <button onClick={handleLogout} className="btn btn-outline-light">
                      Odjava
                    </button>
                  </>
                ) : (
                  <button 
                    onClick={() => {
                      handleToggleLogin();
                      setIsNavCollapsed(true); // Zatvori navbar nakon klika na login
                    }} 
                    className="btn btn-outline-light"
                  >
                    Login
                  </button>
                )}
              </li>
            </ul>
          </div>
        </div>
      </nav>

      {/* Login overlay - OVDJE JE TVOJ GOOGLE LOGIN LINK */}
      {showLogin && !user && (
        <div className="login-overlay bg-secondary bg-opacity-25 py-5">
          <div className="login-box bg-light p-4 text-dark rounded shadow">
            <h5 className="text-center mb-3">Prijava korisnika</h5>
            <div className="text-center">
              <a
                href={`${API_URL}/oauth2/authorization/google`}
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
