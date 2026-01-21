package com.autoservis.services;

import java.util.List;

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