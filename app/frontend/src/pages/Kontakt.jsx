import Mapa from '../components/Mapa';

function Kontakt() {
  return (
    <div className="container">
      <h2>Kontaktirajte nas</h2>
      <p>Naša lokacija:</p>
      <Mapa />
      <p></p>
      <h5>Radno vrijeme</h5>
      <p>Pon - Pet: 8:00 - 17:00</p>
      <p>Tel: +385 98 123 4567</p>
      <p>Email: info@autoservis.hr</p>

    </div>
  );
}

export default Kontakt;
