package com.autoservis.services;

import com.autoservis.models.PrijavaServisa;
import com.autoservis.models.RezervacijaZamjene;
import com.autoservis.models.ZamjenaVozilo;
import com.autoservis.models.Termin;
import com.autoservis.repositories.PrijavaServisaRepository;
import com.autoservis.repositories.RezervacijaZamjeneRepository;
import com.autoservis.repositories.ZamjenaVoziloRepository;
import com.autoservis.repositories.TerminRepository;
import com.autoservis.services.dto.StatsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatsService {

  private static final Logger logger = LoggerFactory.getLogger(StatsService.class);

  private final PrijavaServisaRepository prijavaRepo;
  private final RezervacijaZamjeneRepository rezervRepo;
  private final ZamjenaVoziloRepository zamjenaRepo;
  private final TerminRepository terminRepo;

  public StatsService(PrijavaServisaRepository prijavaRepo, RezervacijaZamjeneRepository rezervRepo, ZamjenaVoziloRepository zamjenaRepo, TerminRepository terminRepo) {
    this.prijavaRepo = prijavaRepo;
    this.rezervRepo = rezervRepo;
    this.zamjenaRepo = zamjenaRepo;
    this.terminRepo = terminRepo;
  }

  public StatsDto gather(LocalDate from, LocalDate to) {
    if (from == null || to == null) {
    
      to = LocalDate.now();
      from = to.minusDays(30);
    }

    StatsDto s = new StatsDto();
    s.from = from; s.to = to;

    LocalDateTime fromDt = from.atStartOfDay();
    LocalDateTime toDt = to.plusDays(1).atStartOfDay().minusSeconds(1);

    
    List<PrijavaServisa> prijave = prijavaRepo.findAll();
    long prijaveCount = prijave.stream().filter(p -> p.getDatumPrijave() != null && (p.getDatumPrijave().isAfter(fromDt) || p.getDatumPrijave().isEqual(fromDt)) && (p.getDatumPrijave().isBefore(toDt) || p.getDatumPrijave().isEqual(toDt))).count();
    s.prijaveCount = prijaveCount;

    
    List<PrijavaServisa> completed = prijave.stream().filter(p -> p.getDatumPredaje() != null && p.getDatumPreuzimanja() != null && (p.getDatumPredaje().isAfter(fromDt) || p.getDatumPredaje().isEqual(fromDt)) && (p.getDatumPreuzimanja().isBefore(toDt) || p.getDatumPreuzimanja().isEqual(toDt))).toList();
    s.completedRepairsCount = completed.size();
    double avgDays = 0.0;
    if (!completed.isEmpty()) {
      double totalDays = completed.stream().mapToDouble(p -> ChronoUnit.SECONDS.between(p.getDatumPredaje(), p.getDatumPreuzimanja()) / 86400.0).sum();
      avgDays = totalDays / completed.size();
    }
    s.averageRepairDays = avgDays;

    
    List<ZamjenaVozilo> zamjene = zamjenaRepo.findAll();
    long rangeDays = ChronoUnit.DAYS.between(from, to) + 1;
    long totalVehicles = zamjene.size() == 0 ? 1 : zamjene.size();

    long totalReservedDays = 0;
    for (ZamjenaVozilo z : zamjene) {
      List<RezervacijaZamjene> overlapping = rezervRepo.findOverlapping(z, from, to);
      for (RezervacijaZamjene r : overlapping) {
        LocalDate od = r.getDatumOd().isBefore(from) ? from : r.getDatumOd();
        LocalDate doDate = r.getDatumDo().isAfter(to) ? to : r.getDatumDo();
        long days = ChronoUnit.DAYS.between(od, doDate) + 1;
        totalReservedDays += days;
      }
    }
    double occupancy = (totalReservedDays / (double)(rangeDays * totalVehicles)) * 100.0;
    s.replacementOccupancyPercent = Math.round(occupancy * 100.0) / 100.0;

  
    List<Termin> termini = terminRepo.findAll();
    List<String> availableSlots = new ArrayList<>();
    long slotsCount = 0;
    for (Termin t : termini) {
      if (t.getDatumVrijeme() != null) {
        LocalDate d = t.getDatumVrijeme().toLocalDate();
        if ((d.isAfter(from) || d.isEqual(from)) &&
        (d.isBefore(to) || d.isEqual(to)) &&
        !t.isZauzet()) 
 {
          availableSlots.add(t.getDatumVrijeme().toString());
          slotsCount++;
        }
      }
    }
    s.availableSlotsCount = slotsCount;
    s.availableSlots = availableSlots;

    logger.info("Computed stats for {}..{} -> prijave={}, completed={}, avgDays={}, occupancy={}%, slots={}", from, to, s.prijaveCount, s.completedRepairsCount, s.averageRepairDays, s.replacementOccupancyPercent, s.availableSlotsCount);

    return s;
  }
}