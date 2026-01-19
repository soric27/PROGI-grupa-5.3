package com.autoservis.interfaces.http.prijava;

import com.autoservis.models.PrijavaServisa;
import com.autoservis.models.Serviser;
import com.autoservis.models.Termin;
import com.autoservis.models.Vozilo;
import com.autoservis.repositories.ServiserRepository;
import com.autoservis.repositories.VoziloRepository;
import com.autoservis.services.PrijavaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/prijave")
public class PrijavaController {

  @Autowired
  private PrijavaService prijavaService;

  @Autowired
  private VoziloRepository voziloRepository;

  @Autowired
  private ServiserRepository serviserRepository;

  public static class CreatePrijavaDto {
    public Long idVozilo;
    public Long idServiser;
    public String napomenaVlasnika;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public LocalDateTime terminDatum;
  }

  @PostMapping
  public ResponseEntity<?> create(@AuthenticationPrincipal Jwt jwt, @RequestBody CreatePrijavaDto dto) throws IOException {
    if (jwt == null) return ResponseEntity.status(401).body("Prijavite se za nastavak.");
    Long idOsoba = jwt.getClaim("id_osoba");
    if (idOsoba == null) return ResponseEntity.status(401).body("Nevažeći token - nedostaje id korisnika.");

    if (dto.idVozilo == null || dto.idServiser == null) {
      return ResponseEntity.badRequest().body("Nedostaju obavezni podaci.");
    }

    Optional<Vozilo> v = voziloRepository.findById(dto.idVozilo);
    if (v.isEmpty()) return ResponseEntity.badRequest().body("Vozilo nije pronađeno");
    if (v.get().getOsoba() == null || !idOsoba.equals(v.get().getOsoba().getIdOsoba())) {
      return ResponseEntity.status(403).body("Nemate pravo prijaviti ovo vozilo.");
    }

    Optional<Serviser> s = serviserRepository.findById(dto.idServiser);
    if (s.isEmpty()) return ResponseEntity.badRequest().body("Serviser nije pronađen");

    Termin termin = null;
    if (dto.terminDatum != null) termin = new Termin(dto.terminDatum);

    PrijavaServisa prijava = new PrijavaServisa(v.get(), s.get(), termin, dto.napomenaVlasnika);

    PrijavaServisa saved = prijavaService.createPrijava(prijava);
    return ResponseEntity.ok(saved);
  }

  public static class UpdatePrijavaDto {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public LocalDateTime newTerminDatum;
    public String status;
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody UpdatePrijavaDto dto) throws IOException, MessagingException {
    if (jwt == null) return ResponseEntity.status(401).body("Prijavite se za nastavak.");
    String uloga = jwt.getClaimAsString("uloga");
    if (uloga == null || !(uloga.equalsIgnoreCase("serviser") || uloga.equalsIgnoreCase("administrator"))) {
      return ResponseEntity.status(403).body("Nemate ovlasti za ovu akciju.");
    }

    Optional<PrijavaServisa> updated = prijavaService.updatePrijava(id, dto.newTerminDatum, dto.status);
    return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }
}
