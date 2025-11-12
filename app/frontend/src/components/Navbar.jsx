import React, { Component } from "react";
import { Link } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import logo from "../assets/auto-servis-logo.png";

class Navbar extends Component {
  constructor(props) {
    super(props);
    this.state = {
      showLogin: false,
    };
  }

  handleToggleLogin = () => {
    this.setState((prevState) => ({ showLogin: !prevState.showLogin }));
  };

  render() {
    const { user } = this.props;
    const { showLogin } = this.state;

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
                  <Link className="nav-link" to="/">
                    Početna
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className="nav-link" to="/vozila">
                    Vozila
                  </Link>
                </li>
                <li className="nav-item">
                  <Link className="nav-link" to="/kontakt">
                    Kontakt
                  </Link>
                </li>
                <li className="nav-item ms-3">
                  {user ? (
                    <>
                      <span className="text-light me-2">Pozdrav, {user.ime}</span>
                      <a href="https://progi-grupa-5-3-j24v.onrender.com/auth/logout" className="btn btn-outline-light">
                        Odjava
                      </a>
                    </>
                  ) : (
                    <button onClick={this.handleToggleLogin} className="btn btn-outline-light">
                      Login
                    </button>
                  )}
                </li>
              </ul>
            </div>
          </div>
        </nav>

        {showLogin && (
          <div className="modal show d-block" tabIndex="-1">
            <div className="modal-dialog modal-dialog-centered">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">Prijava</h5>
                  <button
                    type="button"
                    className="btn-close"
                    onClick={this.handleToggleLogin}
                  ></button>
                </div>
                <div className="modal-body text-center">
                  <a href="https://progi-grupa-5-3-j24v.onrender.com/auth/google" className="btn btn-primary btn-lg w-100">
                    Prijavi se s Googleom
                  </a>
                </div>
              </div>
            </div>
          </div>
        )}
      </>
    );
  }
}

export default Navbar;
