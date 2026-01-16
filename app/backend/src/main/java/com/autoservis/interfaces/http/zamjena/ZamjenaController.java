package com.autoservis.interfaces.http.zamjena;

import com.autoservis.models.RezervacijaZamjene;
import com.autoservis.models.ZamjenaVozilo;
import com.autoservis.services.ZamjenaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

  public static class CreateDto {
    public Long idModel;
    public String registracija;
  }

  @PostMapping
  public ResponseEntity<?> create(@RequestBody CreateDto dto) {
    try {
      ZamjenaVozilo z = zamjenaService.create(dto.idModel, dto.registracija);
      return ResponseEntity.ok(z);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/rezervacije")
  public ResponseEntity<?> reserve(@RequestBody ReserveDto dto) {
    try {
      RezervacijaZamjene rez = zamjenaService.reserve(dto.idPrijava, dto.idZamjena, dto.datumOd, dto.datumDo);
      return ResponseEntity.ok(rez);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(409).body(e.getMessage());
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
