import { useEffect, useState } from 'react';
import axios from 'axios';
import Mapa from '../components/Mapa';

function Kontakt() {
  const [info, setInfo] = useState({ contactEmail: 'info@autoservis.hr', contactPhone: '+385 98 123 4567' });

  useEffect(() => {
    const fetch = () => axios.get('/api/servis').then(r => setInfo(r.data)).catch(e => console.error(e));
    fetch();
    const handler = () => fetch();
    window.addEventListener('servis-updated', handler);
    return () => window.removeEventListener('servis-updated', handler);
  }, []);

  return (
    <div className="container">
      <h2>Kontaktirajte nas</h2>
      <p>Naša lokacija:</p>
      <Mapa />
      <p></p>
      <h5>Radno vrijeme</h5>
      <p>{info.workingHours || 'Pon - Pet: 8:00 - 17:00'}</p>
      <p>Tel: {info.contactPhone}</p>
      <p>Email: {info.contactEmail}</p>

    </div>
  );
}

export default Kontakt;
