import { Link } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import logo from '../assets/auto-servis-logo.png'; 
// (vidi dolje objašnjenje gdje je smjestiti)

function Navbar() {
  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
      <div className="container">
        <Link className="navbar-brand d-flex align-items-center" to="/">
          <img
            src={logo}
            alt="Auto Servis Logo"
            width="80"
            height="80"
            className="me-2"
          />
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
          </ul>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
