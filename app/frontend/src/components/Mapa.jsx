import { GoogleMap, LoadScript, Marker } from '@react-google-maps/api';

function Mapa() {
  const containerStyle = {
    width: '100%',
    height: '400px'
  };

  const center = {
    lat: 45.815399,  
    lng: 15.966568
  };

  return (
    <LoadScript googleMapsApiKey="AIzaSyBh9aZMalJ5ooDAfHUGYCmxH3lpfIgt5qk">
      <GoogleMap
        mapContainerStyle={containerStyle}
        center={center}
        zoom={13}
      >
        <Marker position={center} />
      </GoogleMap>
    </LoadScript>
  );
}

export default Mapa;
