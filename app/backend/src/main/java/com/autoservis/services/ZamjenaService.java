package com.autoservis.services;

import com.autoservis.models.RezervacijaZamjene;
import com.autoservis.models.ZamjenaVozilo;
import com.autoservis.models.PrijavaServisa;
import com.autoservis.repositories.RezervacijaZamjeneRepository;
import com.autoservis.repositories.ZamjenaVoziloRepository;
import com.autoservis.repositories.PrijavaServisaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autoservis.models.Model;
import com.autoservis.repositories.ModelRepository;


import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ZamjenaService {

  private static final Logger logger = LoggerFactory.getLogger(ZamjenaService.class);

  private final ZamjenaVoziloRepository zamjenaRepo;
  private final RezervacijaZamjeneRepository rezervacijaRepo;
  private final PrijavaServisaRepository prijavaRepo;
  private final ModelRepository modelRepo;


  public ZamjenaService(ZamjenaVoziloRepository zamjenaRepo,
                RezervacijaZamjeneRepository rezervacijaRepo,
                PrijavaServisaRepository prijavaRepo,
                ModelRepository modelRepo) {
                this.zamjenaRepo = zamjenaRepo;
                this.rezervacijaRepo = rezervacijaRepo;
                this.prijavaRepo = prijavaRepo;
                this.modelRepo = modelRepo;
}


  public List<ZamjenaVozilo> listAvailable(LocalDate from, LocalDate to) {
    
    if (from == null || to == null) return zamjenaRepo.findByDostupnoTrue();

    
    List<ZamjenaVozilo> candidates = zamjenaRepo.findByDostupnoTrue();
    return candidates.stream().filter(z -> rezervacijaRepo.findOverlapping(z, from, to).isEmpty()).collect(Collectors.toList());
  }

  @Transactional
  public RezervacijaZamjene reserve(Long idPrijava, Long idZamjena, LocalDate from, LocalDate to) {
    if (from == null || to == null || from.isAfter(to)) throw new IllegalArgumentException("Invalid date range");

    PrijavaServisa prijava = prijavaRepo.findById(idPrijava).orElseThrow(() -> new IllegalArgumentException("Prijava not found"));
    ZamjenaVozilo zamjena = zamjenaRepo.findById(idZamjena).orElseThrow(() -> new IllegalArgumentException("Zamjena not found"));

    
    if (zamjena.getDostupno() == null || !zamjena.getDostupno()) throw new IllegalStateException("Zamjensko vozilo nije dostupno");

    
    if (!rezervacijaRepo.findOverlapping(zamjena, from, to).isEmpty()) throw new IllegalStateException("Zamjensko vozilo već je rezervirano u tom periodu");

    RezervacijaZamjene rez = new RezervacijaZamjene(prijava, zamjena, from, to);
    rez = rezervacijaRepo.save(rez);

   
    zamjena.setDostupno(false);
    zamjenaRepo.save(zamjena);

    logger.info("Created reservation {} for zamjena {} and prijava {}; marked zamjena as unavailable", rez.getIdRezervacija(), zamjena.getIdZamjena(), prijava.getIdPrijava());

    return rez;
  }

  @Transactional
  public ZamjenaVozilo create(Long idModel, String registracija) {
    if (idModel == null || registracija == null || registracija.isBlank()) throw new IllegalArgumentException("Invalid input");
    Model model = modelRepo.findById(idModel).orElseThrow(() -> new IllegalArgumentException("Model not found"));
    ZamjenaVozilo z = new ZamjenaVozilo(model, registracija);
    return zamjenaRepo.save(z);
  }

  public List<RezervacijaZamjene> getReservationsForPrijava(Long idPrijava) {
    return rezervacijaRepo.findByPrijava_IdPrijava(idPrijava);
  }

  @Transactional
  public void returnReservation(Long idRezervacija) {
    RezervacijaZamjene rez = rezervacijaRepo.findById(idRezervacija).orElseThrow(() -> new IllegalArgumentException("Rezervacija not found"));
    ZamjenaVozilo zamjena = rez.getZamjena();

    
    zamjena.setDostupno(true);
    zamjenaRepo.save(zamjena);

    
    rezervacijaRepo.delete(rez);

    logger.info("Returned zamjena {} and removed reservation {}", zamjena.getIdZamjena(), rez.getIdRezervacija());
  }
}