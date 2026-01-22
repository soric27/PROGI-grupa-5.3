package com.autoservis.services;

import java.util.List;
import java.time.Year;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoservis.interfaces.http.vozilo.VehicleCreateDto;
import com.autoservis.interfaces.http.vozilo.VehicleDto;
import com.autoservis.models.Model;
import com.autoservis.models.Osoba;
import com.autoservis.models.Vozilo;
import com.autoservis.repositories.ModelRepository;
import com.autoservis.repositories.OsobaRepository;
import com.autoservis.repositories.VoziloRepository;
import com.autoservis.shared.VehicleMapper;

@Service
public class VoziloServiceImpl implements VoziloService {
  private final VoziloRepository vozila;
  private final OsobaRepository osobe;
  private final ModelRepository modeli;

  public VoziloServiceImpl(VoziloRepository v, OsobaRepository o, ModelRepository m){
    this.vozila=v; this.osobe=o; this.modeli=m;
  }

  @Override @Transactional(readOnly = true)
  public List<VehicleDto> getForOsoba(Long idOsoba) {
    return vozila.findByOsoba_IdOsobaOrderByIdVoziloDesc(idOsoba)
      .stream().map(VehicleMapper::toDto).toList();
  }

  @Override @Transactional
  public VehicleDto addForOsoba(Long idOsoba, VehicleCreateDto dto) {
    Osoba o = osobe.findById(idOsoba).orElseThrow(() -> new IllegalArgumentException("Osoba ne postoji"));
    Model m = modeli.findById(dto.id_model()).orElseThrow(() -> new IllegalArgumentException("Model ne postoji"));
    int currentYear = Year.now().getValue();
    if (dto.godina_proizvodnje() == null || dto.godina_proizvodnje() < 1900 || dto.godina_proizvodnje() > currentYear) {
      throw new IllegalArgumentException("Godina proizvodnje mora biti izmedu 1900 i " + currentYear + ".");
    }
    Vozilo saved = vozila.save(new Vozilo(o, m, dto.registracija(), dto.godina_proizvodnje()));
    return VehicleMapper.toDto(saved);
  }

  @Override @Transactional
  public void deleteById(Long idVozilo) {
    vozila.deleteById(idVozilo);
  }

  @Override @Transactional
  public void deleteIfAllowed(Long idVozilo, Long requesterId, boolean isAdmin) {
    var v = vozila.findById(idVozilo).orElseThrow(() -> new IllegalArgumentException("Vozilo ne postoji."));
    if (!isAdmin && !v.getOsoba().getIdOsoba().equals(requesterId)) {
      throw new org.springframework.security.access.AccessDeniedException("Nemate dopuštenje za brisanje ovog vozila.");
    }
    vozila.deleteById(idVozilo);
  }
}
