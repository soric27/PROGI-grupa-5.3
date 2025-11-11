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
  }

  async fetchUser() {
    try {
      const userData = await ApiService.getCurrentUser();
      this.setState({ user: userData });
    } catch (error) {
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
