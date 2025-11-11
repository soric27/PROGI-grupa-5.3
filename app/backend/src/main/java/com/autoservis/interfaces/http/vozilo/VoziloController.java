package main.java.com.autoservis.interfaces.http.vozilo;

import com.autoservis.application.vozilo.VoziloService;
import com.autoservis.interfaces.dto.vozilo.VehicleCreateDto;
import com.autoservis.interfaces.dto.vozilo.VehicleDto;
import com.autoservis.security.SpringOsobaPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/vehicles")
public class VoziloController {
  private final VoziloService service;
  public VoziloController(VoziloService s){ this.service = s; }

  // GET /api/vehicles  (auth required, kao checkAuthentication)
  @GetMapping
  public ResponseEntity<List<VehicleDto>> myVehicles(Authentication auth){
    var idOsoba = requireUserId(auth);
    return ResponseEntity.ok(service.getForOsoba(idOsoba));
  }

  // POST /api/vehicles (auth required)
  @PostMapping
  public ResponseEntity<VehicleDto> create(Authentication auth, @Valid @RequestBody VehicleCreateDto body){
    var idOsoba = requireUserId(auth);
    var created = service.addForOsoba(idOsoba, body);
    return ResponseEntity.status(201).body(created);
  }

  // DELETE /api/vehicles/{id} (samo administrator)
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMINISTRATOR')")
  public ResponseEntity<?> delete(@PathVariable("id") Long id){
    service.deleteById(id);
    return ResponseEntity.ok(new Message("Vozilo " + id + " je obrisano."));
  }

  private Long requireUserId(Authentication auth){
    if (auth == null || !(auth.getPrincipal() instanceof SpringOsobaPrincipal p))
      throw new Unauthorized("Pristup odbijen. Prijavite se za nastavak.");
    var o = p.getOsoba();
    return o.getIdOsoba();
  }

  public record Message(String message){}
  public static class Unauthorized extends RuntimeException {
    public Unauthorized(String m){ super(m); }
  }
}