package com.autoservis.interfaces.http.prijava;

import com.autoservis.models.PrijavaServisa;
import com.autoservis.models.Termin;
import com.autoservis.models.Vozilo;
import com.autoservis.models.Serviser;
import com.autoservis.repositories.ServiserRepository;
import com.autoservis.repositories.VoziloRepository;
import com.autoservis.services.PdfService;
import com.autoservis.services.PrijavaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
  private ServiserRepository serviserRepository;

  @Autowired
  private VoziloRepository voziloRepository;

  @Autowired
  private PdfService pdfService;

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
    if (dto.terminDatum != null) termin = new Termin(dto.terminDatum);

    Optional<Serviser> s = serviserRepository.findById(dto.idServiser);
    if (s.isEmpty()) return ResponseEntity.badRequest().body("Serviser nije pronađen");


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
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdatePrijavaDto dto) {
    Optional<PrijavaServisa> updated = prijavaService.updatePrijava(id, dto.newTerminDatum, dto.status);
    return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }
}
