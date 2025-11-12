import React, { Component } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Vozila from "./pages/Vozila";
import Kontakt from "./pages/Kontakt";
import ApiService from "./services/ApiService";

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      user: null,
    };
  }

  componentDidMount() {
    this.fetchUser();
    
    // ✅ DODAJ OVO - Provjeri ako je login=success u URL-u
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('login') === 'success') {
      // Ponovno fetchaj korisnika nakon logina
      setTimeout(() => this.fetchUser(), 500);
      
      // Očisti URL parametar
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }

  async fetchUser() {
    try {
      const userData = await ApiService.getCurrentUser();
      this.setState({ user: userData });
      console.log("User logged in:", userData); // za debug
    } catch (error) {
      console.log("User not logged in");
      this.setState({ user: null });
    }
  }

  render() {
    const { user } = this.state;

    return (
      <Router>
        <Navbar user={user} />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/vozila" element={<Vozila user={user} />} />
          <Route path="/kontakt" element={<Kontakt />} />
        </Routes>
      </Router>
    );
  }
}

export default App;
