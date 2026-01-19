import 'bootstrap/dist/css/bootstrap.min.css';
import '../styles/Home.css';
import { Link } from 'react-router-dom';

function Home() {
  return (
    <>
      {/* Hero sekcija */}
      <div className="hero d-flex align-items-center justify-content-center text-center mb-0">
        <div className="hero-content">
          <h1 className="display-4 fw-bold text-light">Dobrodošli u Auto Servis</h1>
          <p className="lead text-light mb-4">
            Digitalna prijava vozila i praćenje statusa popravka - brzo, jednostavno i moderno.
          </p>
          <Link to="/vozila" className="btn btn-primary btn-lg">
            Prijavi vozilo
          </Link>
        </div>
      </div>
      
      {/* O nama sekcija */}
      <div className="container my-5">
        <h3 className="fw-bold mb-3 text-primary">O nama</h3>
        <p style={{ fontSize: "1.15rem", lineHeight: "1.7" }}>
          Auto Servis MK2 je moderan servis s dugogodišnjom tradicijom i timom stručnih mehaničara, električara i dijagnostičara. Naša misija je učiniti održavanje i popravke vašeg vozila lakšim, bržim i transparentnijim nego ikad prije.
          <br /><br />
          Koristimo najnoviju tehnologiju za dijagnostiku i praćenje statusa popravka, a naš online sustav omogućuje vam da prijavite vozilo, rezervirate termin i pratite svaku fazu servisa iz udobnosti doma.
          <br /><br />
          Vjerujemo u iskren pristup, vrhunsku uslugu i sigurnost naših klijenata. Zahvaljujući zamjenskim vozilima i digitalnom praćenju, nikada ne ostajete bez prijevoza ili informacija.
          <br /><br />
          Vaše povjerenje nam je na prvom mjestu - dobrodošli u servis gdje se vozila popravljaju s osmijehom!
        </p>
      </div>
    </>
  );
}

export default Home;
