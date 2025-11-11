package main.java.com.autoservis.shared;

import com.autoservis.domain.vozilo.Vozilo;
import com.autoservis.interfaces.dto.vozilo.VehicleDto;

public class VehicleMapper {
  public static VehicleDto toDto(Vozilo v){
    var m = v.getModel();
    var marka = (m.getMarka()!=null) ? m.getMarka().getNaziv() : null;
    return new VehicleDto(
      v.getIdVozilo(),
      m.getIdModel(),
      v.getRegistracija(),
      v.getGodinaProizvodnje(),
      m.getNaziv(),
      marka
    );
  }
}