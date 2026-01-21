package com.autoservis.interfaces.http.prijava;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoservis.interfaces.dto.PrijavaDetalleDto;
import com.autoservis.models.PrijavaServisa;
import com.autoservis.models.Serviser;
import com.autoservis.models.Termin;
import com.autoservis.models.Vozilo;
import com.autoservis.repositories.VoziloRepository;
import com.autoservis.services.PdfService;
import com.autoservis.services.PrijavaService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/prijave")
public class PrijavaController {

  @Autowired
  private PrijavaService prijavaService;

  @Autowired
  private VoziloRepository voziloRepository;

  @Autowired
  private PdfService pdfService;

  @Autowired
  private com.autoservis.repositories.ServiserRepository serviserRepository;

  public static class CreatePrijavaDto {
    public Long idVozilo;
    public Long idServiser;
    public String napomenaVlasnika;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public LocalDateTime terminDatum;
  }

  @PostMapping
  public ResponseEntity<?> create(@RequestBody CreatePrijavaDto dto) throws IOException {
    Optional<Vozilo> v = voziloRepository.findById(dto.idVozilo);
    if (v.isEmpty()) return ResponseEntity.badRequest().body("Vozilo nije pronađeno");

    Termin termin = null;
    Serviser serviser = null;
    if (dto.idServiser != null) {
      serviser = serviserRepository.findById(dto.idServiser).orElseThrow(() -> new IllegalArgumentException("Serviser nije pronađen"));
    }

    if (dto.terminDatum != null) {
      termin = new Termin(dto.terminDatum);
      if (serviser != null) termin.setServiser(serviser);
    }

    PrijavaServisa prijava = new PrijavaServisa(v.get(), serviser, termin, dto.napomenaVlasnika);

    // if termin was created, it needs to be saved via PrijavaService which will create a termin entity
    PrijavaServisa saved = prijavaService.createPrijava(prijava);

    return ResponseEntity.ok(saved);
  }

  public static class UpdatePrijavaDto {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public LocalDateTime newTerminDatum;
    public String status;
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdatePrijavaDto dto, @AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) throws IOException, MessagingException {
    if (jwt == null) return ResponseEntity.status(401).body("Niste prijavljeni.");
    Long idOsoba = jwt.getClaim("id_osoba");
    if (idOsoba == null) return ResponseEntity.status(401).body("Nevažeći token.");
    boolean isAdmin = "administrator".equalsIgnoreCase((String) jwt.getClaim("uloga"));
    boolean isServiser = "serviser".equalsIgnoreCase((String) jwt.getClaim("uloga"));

    try {
      Optional<PrijavaDetalleDto> updated = prijavaService.updatePrijava(id, dto.newTerminDatum, dto.status, idOsoba, isAdmin, isServiser);
      return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    } catch (org.springframework.security.access.AccessDeniedException e) {
      return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
    }
  }
}
