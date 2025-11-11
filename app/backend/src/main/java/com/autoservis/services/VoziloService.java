package main.java.com.autoservis.services;

import com.autoservis.interfaces.dto.vozilo.VehicleCreateDto;
import com.autoservis.interfaces.dto.vozilo.VehicleDto;

import java.util.List;

public interface VoziloService {
  List<VehicleDto> getForOsoba(Long idOsoba);
  VehicleDto addForOsoba(Long idOsoba, VehicleCreateDto dto);
  void deleteById(Long idVozilo);
}