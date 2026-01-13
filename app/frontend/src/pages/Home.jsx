import 'bootstrap/dist/css/bootstrap.min.css';
import '../styles/Home.css';
import { Link } from 'react-router-dom';
import { useEffect, useState } from 'react';
import axios from 'axios';

function Home() {
  const [about, setAbout] = useState(`Auto Servis MK2 je moderan servis s dugogodišnjom tradicijom i timom stručnih mehaničara, električara i dijagnostičara. Naša misija je učiniti održavanje i popravke vašeg vozila lakšim, bržim i transparentnijim nego ikad prije.`);

  useEffect(() => {
    axios.get('/api/servis').then(r => setAbout(r.data.aboutText)).catch(e => console.error(e));
  }, []);

  return (
    <>
      {/* Hero sekcija */}
      <div className="hero d-flex align-items-center justify-content-center text-center mb-0">
        <div className="hero-content">
          <h1 className="display-4 fw-bold text-light">Dobrodošli u Auto Servis</h1>
          <p className="lead text-light mb-4">
            Digitalna prijava vozila i praćenje statusa popravka — brzo, jednostavno i moderno.
          </p>
          <Link to="/vozila" className="btn btn-primary btn-lg">
            Prijavi vozilo
          </Link>
        </div>
      </div>
      
      {/* O nama sekcija */}
      <div className="container my-5">
        <h3 className="fw-bold mb-3 text-primary">O nama</h3>
        <div style={{ fontSize: "1.15rem", lineHeight: "1.7" }} dangerouslySetInnerHTML={{ __html: about }} />
      </div>
    </>
  );
}

export default Home;
