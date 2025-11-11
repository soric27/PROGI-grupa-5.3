package com.autoservis.application.vozilo;

import com.autoservis.domain.model.Model;
import com.autoservis.domain.osoba.Osoba;
import com.autoservis.domain.vozilo.Vozilo;
import com.autoservis.infrastructure.persistence.model.ModelRepository;
import com.autoservis.infrastructure.persistence.osoba.OsobaRepository;
import com.autoservis.infrastructure.persistence.vozilo.VoziloRepository;
import com.autoservis.interfaces.dto.vozilo.VehicleCreateDto;
import com.autoservis.interfaces.dto.vozilo.VehicleDto;
import com.autoservis.shared.mapper.VehicleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}