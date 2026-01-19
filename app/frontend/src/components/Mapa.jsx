import { useEffect, useState } from 'react';
import axios from 'axios';
import { GoogleMap, LoadScript, Marker } from '@react-google-maps/api';

function Mapa() {
  const [location, setLocation] = useState(null);
  const [loading, setLoading] = useState(true);

  const containerStyle = {
    width: '100%',
    height: '400px'
  };

  useEffect(() => {
    const fetchLocation = async () => {
      try {
        const response = await axios.get('/api/servis');
        const { latitude, longitude } = response.data;
        if (latitude && longitude) {
          setLocation({
            lat: latitude,
            lng: longitude
          });
        } else {
          // Fallback ako nema koordinata
          setLocation({
            lat: 45.815399,
            lng: 15.966568
          });
        }
      } catch (error) {
        console.error('Greška pri dohvaćanju lokacije servisa:', error);
        // Fallback lokacija
        setLocation({
          lat: 45.815399,
          lng: 15.966568
        });
      } finally {
        setLoading(false);
      }
    };

    fetchLocation();
  }, []);

  if (loading) {
    return <div style={{ width: '100%', height: '400px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>Učitavanje karte...</div>;
  }

  return (
    <LoadScript googleMapsApiKey="AIzaSyBh9aZMalJ5ooDAfHUGYCmxH3lpfIgt5qk">
      <GoogleMap
        mapContainerStyle={containerStyle}
        center={location}
        zoom={13}
      >
        <Marker position={location} />
      </GoogleMap>
    </LoadScript>
  );
}

export default Mapa;
