package com.autoservis.services;

import com.autoservis.interfaces.http.vozilo.VehicleCreateDto;
import com.autoservis.interfaces.http.vozilo.VehicleDto;

import java.util.List;

public interface VoziloService {
  List<VehicleDto> getForOsoba(Long idOsoba);
  VehicleDto addForOsoba(Long idOsoba, VehicleCreateDto dto);
  void deleteById(Long idVozilo);
}