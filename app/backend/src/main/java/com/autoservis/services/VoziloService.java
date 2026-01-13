package com.autoservis.services;

import java.util.List;

import com.autoservis.interfaces.http.vozilo.VehicleCreateDto;
import com.autoservis.interfaces.http.vozilo.VehicleDto;

public interface VoziloService {
  List<VehicleDto> getForOsoba(Long idOsoba);
  VehicleDto addForOsoba(Long idOsoba, VehicleCreateDto dto);
  void deleteById(Long idVozilo);
  // Delete allowed for owner or admin
  void deleteIfAllowed(Long idVozilo, Long requesterId, boolean isAdmin);
}