package com.autoservis.interfaces.http;

import com.autoservis.interfaces.dto.NapomenaCreateDto;
import com.autoservis.interfaces.dto.PrijavaDetalleDto;
import com.autoservis.interfaces.dto.PrijavaServisaCreateDto;
import com.autoservis.interfaces.dto.ServiserDto;
import com.autoservis.interfaces.dto.StatusUpdateDto;
import com.autoservis.interfaces.dto.TerminDto;
import com.autoservis.services.PrijavaServisaService;
import com.autoservis.services.ServiserService;
import com.autoservis.services.TerminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final TerminService terminService;
    private final ServiserService serviserService;
    private final PrijavaServisaService prijavaService;

    public AppointmentController(TerminService terminService, ServiserService serviserService, PrijavaServisaService prijavaService) {
        this.terminService = terminService;
        this.serviserService = serviserService;
        this.prijavaService = prijavaService;
    }

    // Endpoint za dohvaćanje slobodnih termina
    @GetMapping("/termini")
    public ResponseEntity<List<TerminDto>> getSlobodniTermini() {
        return ResponseEntity.ok(terminService.getSlobodniTermini());
    }

    // Endpoint za dohvaćanje svih servisera
    @GetMapping("/serviseri")
    public ResponseEntity<List<ServiserDto>> getSviServiseri() {
        return ResponseEntity.ok(serviserService.getSviServiseri());
    }

    // Endpoint za kreiranje nove prijave servisa
    @PostMapping("/prijave")
    public ResponseEntity<?> createPrijava(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PrijavaServisaCreateDto dto
    ) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Prijavite se za nastavak."));
        }

        Long idVlasnika = jwt.getClaim("id_osoba");
        if (idVlasnika == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }

        prijavaService.createPrijava(dto, idVlasnika);

        return ResponseEntity.status(201).body(Map.of("message", "Prijava za servis je uspješno kreirana."));
    }

     // Dohvati sve prijave za prijavljenog KORISNIKA
    @GetMapping("/prijave/moje")
    public ResponseEntity<List<PrijavaDetalleDto>> getMojePrijave(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(prijavaService.getPrijaveForKorisnik(idOsoba));
    }

    // Dohvati sve prijave za prijavljenog SERVISERA
    @GetMapping("/prijave/dodijeljene")
    @PreAuthorize("hasAnyRole('SERVISER', 'ADMINISTRATOR')")
    public ResponseEntity<List<PrijavaDetalleDto>> getDodijeljenePrijave(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(prijavaService.getPrijaveForServiser(idOsoba));
    }
    
    // Promijeni status prijave - samo za SERVISERE
    @PatchMapping("/prijave/{id}/status")
    @PreAuthorize("hasAnyRole('SERVISER', 'ADMINISTRATOR')")
    public ResponseEntity<?> updateStatus(
            @PathVariable("id") Long idPrijava,
            @Valid @RequestBody StatusUpdateDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Prijavite se za nastavak."));
        }

        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }
        prijavaService.updateStatus(idPrijava, dto.noviStatus(), idOsoba);
        return ResponseEntity.ok(Map.of("message", "Status prijave je ažuriran."));
    }
    
    // Dodaj napomenu na prijavu - samo za SERVISERE
    @PostMapping("/prijave/{id}/napomene")
    @PreAuthorize("hasAnyRole('SERVISER', 'ADMINISTRATOR')")
    public ResponseEntity<?> addNapomena(
            @PathVariable("id") Long idPrijava,
            @Valid @RequestBody NapomenaCreateDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Prijavite se za nastavak."));
        }

        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }
        prijavaService.addNapomena(idPrijava, dto, idOsoba);
        return ResponseEntity.status(201).body(Map.of("message", "Napomena je dodana."));
    }
}
