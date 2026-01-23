package com.autoservis.interfaces.http.zamjena;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import com.autoservis.models.RezervacijaZamjene;
import com.autoservis.models.ZamjenaVozilo;
import com.autoservis.services.ZamjenaService;

@RestController
@RequestMapping("/api/zamjene")
public class ZamjenaController {

  @Autowired
  private ZamjenaService zamjenaService;

  @Autowired
  private com.autoservis.repositories.ZamjenaVoziloRepository zamjenaRepo;

  @Autowired
  private com.autoservis.repositories.ModelRepository modelRepo;

  private static final Logger logger = LoggerFactory.getLogger(ZamjenaController.class);

  // helper to read string claim safely (handles String, List, numeric values)
  private String claimAsString(Jwt jwt, String name) {
    Object raw = jwt == null ? null : jwt.getClaim(name);
    if (raw == null) return null;
    if (raw instanceof String) return (String) raw;
    if (raw instanceof java.util.List) {
      java.util.List<?> l = (java.util.List<?>) raw;
      return l.isEmpty() ? null : String.valueOf(l.get(0));
    }
    return String.valueOf(raw);
  }

  private Long claimAsLong(Jwt jwt, String name) {
    Object raw = jwt == null ? null : jwt.getClaim(name);
    if (raw == null) return null;
    if (raw instanceof Number) return ((Number) raw).longValue();
    try { return Long.parseLong(String.valueOf(raw)); } catch (Exception e) { return null; }
  }

  @GetMapping
  public ResponseEntity<?> listAvailable(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    java.util.List<com.autoservis.interfaces.http.zamjena.ZamjenaDto> list = zamjenaService.listAvailable(from, to);
    return ResponseEntity.ok(list);
  }

  // Admin: list all replacement vehicles (including unavailable)
  @GetMapping("/all")
  public ResponseEntity<?> listAll(@AuthenticationPrincipal Jwt jwt) {
    if (jwt == null) return ResponseEntity.status(401).body("Niste prijavljeni.");
    boolean isAdmin = "administrator".equalsIgnoreCase(claimAsString(jwt, "uloga"));
    if (!isAdmin) return ResponseEntity.status(403).body("Samo administrator može vidjeti sve zamjenske liste.");
    try {
      return ResponseEntity.ok(zamjenaService.listAll());
    } catch (Exception e) {
      logger.error("Error listing replacement vehicles", e);
      return ResponseEntity.status(500).body(e.getMessage() == null ? "Internal server error" : e.getMessage());
    }
  }

  public static class CreateZamjenaDto {
    public Long idModel;
    public String registracija;
    public Boolean dostupno;
  }

  @PostMapping
  @Transactional
  public ResponseEntity<?> createZamjena(@RequestBody CreateZamjenaDto dto, @AuthenticationPrincipal Jwt jwt) {
    if (jwt == null) return ResponseEntity.status(401).body("Niste prijavljeni.");
    boolean isAdmin = "administrator".equalsIgnoreCase(claimAsString(jwt, "uloga"));
    if (!isAdmin) return ResponseEntity.status(403).body("Samo administrator može dodavati zamjenska vozila.");
    try {
      String registracija = dto.registracija == null ? "" : dto.registracija.trim();
      if (registracija.isEmpty()) return ResponseEntity.badRequest().body("Registracija je obavezna");
      if (zamjenaRepo.existsByRegistracijaIgnoreCase(registracija)) return ResponseEntity.status(409).body("Registracija već postoji");
      com.autoservis.models.Model m = modelRepo.findById(dto.idModel).orElseThrow(() -> new IllegalArgumentException("Model nije pronađen"));
      ZamjenaVozilo z = new ZamjenaVozilo(m, registracija);
      if (dto.dostupno != null) z.setDostupno(dto.dostupno);
      z = zamjenaRepo.save(z);

      // map to DTO to avoid lazy serialization issues
      Long idModel = z.getModel() != null ? z.getModel().getIdModel() : null;
      String modelNaziv = z.getModel() != null ? z.getModel().getNaziv() : null;
      String markaNaziv = (z.getModel() != null && z.getModel().getMarka() != null) ? z.getModel().getMarka().getNaziv() : null;
      com.autoservis.interfaces.http.zamjena.ZamjenaDto dtoOut = new com.autoservis.interfaces.http.zamjena.ZamjenaDto(z.getIdZamjena(), idModel, z.getRegistracija(), z.getDostupno(), modelNaziv, markaNaziv);
      return ResponseEntity.ok(dtoOut);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (DataIntegrityViolationException e) {
      // fallback: unique constraint on registracija violated
      return ResponseEntity.status(409).body("Registracija već postoji");
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteZamjena(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
    if (jwt == null) return ResponseEntity.status(401).body("Niste prijavljeni.");
    boolean isAdmin = "administrator".equalsIgnoreCase(claimAsString(jwt, "uloga"));
    if (!isAdmin) return ResponseEntity.status(403).body("Samo administrator može brisati zamjenska vozila.");
    try {
      zamjenaService.delete(id);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  public static class ReserveDto {
    public Long idPrijava;
    public Long idZamjena;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    public LocalDate datumOd;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    public LocalDate datumDo;
  }

  @PostMapping("/rezervacije")
  public ResponseEntity<?> reserve(@RequestBody ReserveDto dto, @AuthenticationPrincipal Jwt jwt) {
    try {
      if (jwt == null) return ResponseEntity.status(401).body("Niste prijavljeni.");
      Long idOsoba = claimAsLong(jwt, "id_osoba");
      if (idOsoba == null) return ResponseEntity.status(401).body("Nevažeći token.");
      boolean isAdmin = "administrator".equalsIgnoreCase(claimAsString(jwt, "uloga"));
      RezervacijaZamjene rez = zamjenaService.reserveWithAuth(dto.idPrijava, dto.idZamjena, dto.datumOd, dto.datumDo, idOsoba, isAdmin);
      return ResponseEntity.ok(java.util.Map.of(
          "idRezervacija", rez.getIdRezervacija(),
          "message", "Zamjensko vozilo rezervirano."
      ));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(e.getMessage());
    } catch (org.springframework.security.access.AccessDeniedException e) {
      return ResponseEntity.status(403).body(e.getMessage());
    }
  }

  @PostMapping("/rezervacije/{id}/vrati")
  public ResponseEntity<?> returnReservation(@PathVariable Long id) {
    try {
      zamjenaService.returnReservation(id);
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/prijava/{id}/rezervacije")
  public ResponseEntity<?> getForPrijava(@PathVariable Long id) {
    List<RezervacijaZamjene> list = zamjenaService.getReservationsForPrijava(id);
    return ResponseEntity.ok(list);
  }
}
