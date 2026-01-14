package com.autoservis.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoservis.models.PrijavaServisa;
import com.autoservis.models.RezervacijaZamjene;
import com.autoservis.models.ZamjenaVozilo;
import com.autoservis.repositories.PrijavaServisaRepository;
import com.autoservis.repositories.RezervacijaZamjeneRepository;
import com.autoservis.repositories.ZamjenaVoziloRepository;

@Service
public class ZamjenaService {

  private static final Logger logger = LoggerFactory.getLogger(ZamjenaService.class);

  private final ZamjenaVoziloRepository zamjenaRepo;
  private final RezervacijaZamjeneRepository rezervacijaRepo;
  private final PrijavaServisaRepository prijavaRepo;

  public ZamjenaService(ZamjenaVoziloRepository zamjenaRepo, RezervacijaZamjeneRepository rezervacijaRepo, PrijavaServisaRepository prijavaRepo) {
    this.zamjenaRepo = zamjenaRepo;
    this.rezervacijaRepo = rezervacijaRepo;
    this.prijavaRepo = prijavaRepo;
  }

  @Transactional(readOnly = true)
  public java.util.List<com.autoservis.interfaces.http.zamjena.ZamjenaDto> listAvailable(LocalDate from, LocalDate to) {
    java.util.List<ZamjenaVozilo> list;
    // if no range provided, return all marked as dostupno
    if (from == null || to == null) {
      list = zamjenaRepo.findByDostupnoTrue();
    } else {
      // otherwise filter those without overlapping reservations in given range and marked dostupno
      java.util.List<ZamjenaVozilo> candidates = zamjenaRepo.findByDostupnoTrue();
      list = candidates.stream().filter(z -> rezervacijaRepo.findOverlapping(z, from, to).isEmpty()).collect(Collectors.toList());
    }

    // map to DTOs while still in transaction
    java.util.List<com.autoservis.interfaces.http.zamjena.ZamjenaDto> dtos = new java.util.ArrayList<>();
    for (ZamjenaVozilo z : list) {
      Long idModel = z.getModel() != null ? z.getModel().getIdModel() : null;
      String modelNaziv = z.getModel() != null ? z.getModel().getNaziv() : null;
      String markaNaziv = (z.getModel() != null && z.getModel().getMarka() != null) ? z.getModel().getMarka().getNaziv() : null;
      dtos.add(new com.autoservis.interfaces.http.zamjena.ZamjenaDto(z.getIdZamjena(), idModel, z.getRegistracija(), z.getDostupno(), modelNaziv, markaNaziv));
    }
    return dtos;
  }

  // Admin helpers
  @Transactional(readOnly = true)
  public java.util.List<com.autoservis.interfaces.http.zamjena.ZamjenaDto> listAll() {
    java.util.List<ZamjenaVozilo> list = zamjenaRepo.findAll();
    // map to DTOs while still in transaction to allow lazy access
    java.util.List<com.autoservis.interfaces.http.zamjena.ZamjenaDto> dtos = new java.util.ArrayList<>();
    for (ZamjenaVozilo z : list) {
      Long idModel = z.getModel() != null ? z.getModel().getIdModel() : null;
      String modelNaziv = z.getModel() != null ? z.getModel().getNaziv() : null;
      String markaNaziv = (z.getModel() != null && z.getModel().getMarka() != null) ? z.getModel().getMarka().getNaziv() : null;
      dtos.add(new com.autoservis.interfaces.http.zamjena.ZamjenaDto(z.getIdZamjena(), idModel, z.getRegistracija(), z.getDostupno(), modelNaziv, markaNaziv));
    }
    return dtos;
  }



  @Transactional
  public void delete(Long id) {
    ZamjenaVozilo z = zamjenaRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Zamjensko vozilo nije pronađeno"));
    zamjenaRepo.delete(z);
  }

  @Transactional
  public RezervacijaZamjene reserve(Long idPrijava, Long idZamjena, LocalDate from, LocalDate to) {
    if (from == null || to == null || from.isAfter(to)) throw new IllegalArgumentException("Invalid date range");

    PrijavaServisa prijava = prijavaRepo.findById(idPrijava).orElseThrow(() -> new IllegalArgumentException("Prijava not found"));
    ZamjenaVozilo zamjena = zamjenaRepo.findById(idZamjena).orElseThrow(() -> new IllegalArgumentException("Zamjena not found"));

    // check availability flag
    if (zamjena.getDostupno() == null || !zamjena.getDostupno()) throw new IllegalStateException("Zamjensko vozilo nije dostupno");

    // check overlapping reservations
    if (!rezervacijaRepo.findOverlapping(zamjena, from, to).isEmpty()) throw new IllegalStateException("Zamjensko vozilo već je rezervirano u tom periodu");

    RezervacijaZamjene rez = new RezervacijaZamjene(prijava, zamjena, from, to);
    rez = rezervacijaRepo.save(rez);

    // mark the replacement vehicle as unavailable immediately
    zamjena.setDostupno(false);
    zamjenaRepo.save(zamjena);

    logger.info("Created reservation {} for zamjena {} and prijava {}; marked zamjena as unavailable", rez.getIdRezervacija(), zamjena.getIdZamjena(), prijava.getIdPrijava());

    return rez;
  }

  // Reserve with authorization: only owner of prijava or admin may reserve for a prijava
  @Transactional
  public RezervacijaZamjene reserveWithAuth(Long idPrijava, Long idZamjena, LocalDate from, LocalDate to, Long requesterId, boolean isAdmin) {
    PrijavaServisa prijava = prijavaRepo.findById(idPrijava).orElseThrow(() -> new IllegalArgumentException("Prijava not found"));
    if (!isAdmin && !prijava.getVozilo().getOsoba().getIdOsoba().equals(requesterId)) {
      throw new org.springframework.security.access.AccessDeniedException("Nemate ovlasti rezervirati zamjensko vozilo za ovu prijavu.");
    }
    return reserve(idPrijava, idZamjena, from, to);
  }

  public List<RezervacijaZamjene> getReservationsForPrijava(Long idPrijava) {
    return rezervacijaRepo.findByPrijava_IdPrijava(idPrijava);
  }

  @Transactional
  public void returnReservation(Long idRezervacija) {
    RezervacijaZamjene rez = rezervacijaRepo.findById(idRezervacija).orElseThrow(() -> new IllegalArgumentException("Rezervacija not found"));
    ZamjenaVozilo zamjena = rez.getZamjena();

    // mark vehicle available
    zamjena.setDostupno(true);
    zamjenaRepo.save(zamjena);

    // delete reservation as it's finished
    rezervacijaRepo.delete(rez);

    logger.info("Returned zamjena {} and removed reservation {}", zamjena.getIdZamjena(), rez.getIdRezervacija());
  }
}