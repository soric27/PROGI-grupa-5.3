package com.autoservis.interfaces.http.zamjena;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autoservis.models.RezervacijaZamjene;
import com.autoservis.models.ZamjenaVozilo;
import com.autoservis.services.ZamjenaService;

@RestController
@RequestMapping("/api/zamjene")
public class ZamjenaController {

  @Autowired
  private ZamjenaService zamjenaService;

  @GetMapping
  public ResponseEntity<?> listAvailable(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    List<ZamjenaVozilo> list = zamjenaService.listAvailable(from, to);
    return ResponseEntity.ok(list);
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
      Long idOsoba = jwt.getClaim("id_osoba");
      if (idOsoba == null) return ResponseEntity.status(401).body("Nevažeći token.");
      boolean isAdmin = "administrator".equalsIgnoreCase((String) jwt.getClaim("uloga"));
      boolean isServiser = "serviser".equalsIgnoreCase((String) jwt.getClaim("uloga"));
      RezervacijaZamjene rez = zamjenaService.reserveWithAuth(dto.idPrijava, dto.idZamjena, dto.datumOd, dto.datumDo, idOsoba, isAdmin, isServiser);
      return ResponseEntity.ok(rez);
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
