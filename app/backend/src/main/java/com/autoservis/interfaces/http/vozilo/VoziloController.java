package com.autoservis.interfaces.http.vozilo;

import com.autoservis.services.VoziloService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VoziloController {
    private final VoziloService service;

    public VoziloController(VoziloService s) {
        this.service = s;
    }

    // GET /api/vehicles - dohvati vozila trenutnog korisnika
    @GetMapping
    public ResponseEntity<List<VehicleDto>> myVehicles(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long idOsoba = requireUserId(jwt);
        return ResponseEntity.ok(service.getForOsoba(idOsoba));
    }

    // POST /api/vehicles - dodaj novo vozilo za trenutnog korisnika
    @PostMapping
    public ResponseEntity<VehicleDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody VehicleCreateDto body
    ) {
        Long idOsoba = requireUserId(jwt);
        var created = service.addForOsoba(idOsoba, body);
        return ResponseEntity.status(201).body(created);
    }

    // DELETE /api/vehicles/{id} - samo administrator
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        service.deleteById(id);
        return ResponseEntity.ok(new Message("Vozilo " + id + " je obrisano."));
    }

    // Helper metoda za dohvat id_osoba iz JWT tokena
    private Long requireUserId(Jwt jwt) {
        if (jwt == null) {
            throw new Unauthorized("Pristup odbijen. Prijavite se za nastavak.");
        }

        // Dohvati id_osoba iz JWT claima
        Long idOsoba = jwt.getClaim("id_osoba");
        
        if (idOsoba == null) {
            throw new Unauthorized("Nevažeći token - nedostaje id korisnika.");
        }

        return idOsoba;
    }

    // DTO i Exception classes
    public record Message(String message) {}

    public static class Unauthorized extends RuntimeException {
        public Unauthorized(String m) {
            super(m);
        }
    }
}
