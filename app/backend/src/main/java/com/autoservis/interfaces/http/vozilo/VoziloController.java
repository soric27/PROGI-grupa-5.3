package com.autoservis.interfaces.http.vozilo;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoservis.services.VoziloService;

import jakarta.validation.Valid;

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

    // POST /api/vehicles - dodaj novo vozilo za trenutnog korisnika (samo KORISNIK)
    @PostMapping
    @PreAuthorize("hasRole('KORISNIK')")
    public ResponseEntity<VehicleDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody VehicleCreateDto body
    ) {
        Long idOsoba = requireUserId(jwt);
        var created = service.addForOsoba(idOsoba, body);
        return ResponseEntity.status(201).body(created);
    }

    // DELETE /api/vehicles/{id} - owner or administrator
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        Long idOsoba = requireUserId(jwt);
        boolean isAdmin = "administrator".equalsIgnoreCase((String) jwt.getClaim("uloga"));
        service.deleteIfAllowed(id, idOsoba, isAdmin);
        return ResponseEntity.ok(new Message("Vozilo " + id + " je obrisano."));
    }

    // GET /api/vehicles/for/{osobaId} - samo administrator može dohvatiti vozila druge osobe
    @GetMapping("/for/{osobaId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<VehicleDto>> getForUser(@PathVariable("osobaId") Long osobaId) {
        return ResponseEntity.ok(service.getForOsoba(osobaId));
    }

    // POST /api/vehicles/for/{osobaId} - administrator dodaje vozilo za određenog korisnika
    @PostMapping("/for/{osobaId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<VehicleDto> createForUser(@PathVariable("osobaId") Long osobaId, @Valid @RequestBody VehicleCreateDto body) {
        var created = service.addForOsoba(osobaId, body);
        return ResponseEntity.status(201).body(created);
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
