package com.autoservis.interfaces.http;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autoservis.interfaces.dto.NapomenaCreateDto;
import com.autoservis.interfaces.dto.PrijavaServisaCreateDto;
import com.autoservis.interfaces.dto.ServiserDto;
import com.autoservis.interfaces.dto.StatusUpdateDto;
import com.autoservis.services.PrijavaServisaService;
import com.autoservis.services.ServiserService;
import com.autoservis.services.TerminService;

import jakarta.validation.Valid;

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

    // Endpoint za dohvaćanje slobodnih termina (samo za prijavljene korisnike)
    @GetMapping("/termini")
    public ResponseEntity<?> getSlobodniTermini(@RequestParam(required = false) Long serviserId,
                                                @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Niste prijavljeni."));
        }
        return ResponseEntity.ok(terminService.getSlobodniTermini(serviserId));
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
            return ResponseEntity.status(401).body(Map.of("message", "Niste prijavljeni."));
        }
        
        Long idVlasnika = jwt.getClaim("id_osoba");
        if (idVlasnika == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }
        
        var created = prijavaService.createPrijava(dto, idVlasnika);
        
        return ResponseEntity.status(201).body(created);
    }

    // Dohvati sve prijave za prijavljenog KORISNIKA
    @GetMapping("/prijave/moje")
    public ResponseEntity<?> getMojePrijave(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Niste prijavljeni."));
        }
        
        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }
        
        return ResponseEntity.ok(prijavaService.getPrijaveForKorisnik(idOsoba));
    }

    // Dohvati sve prijave za prijavljenog SERVISERA
    @GetMapping("/prijave/dodijeljene")
    @PreAuthorize("hasAnyRole('SERVISER', 'ADMINISTRATOR')")
    public ResponseEntity<?> getDodijeljenePrijave(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Niste prijavljeni."));
        }
        
        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
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
            return ResponseEntity.status(401).body(Map.of("message", "Niste prijavljeni."));
        }
        
        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }
        
        prijavaService.updateStatus(idPrijava, dto.noviStatus(), idOsoba);
        return ResponseEntity.ok(Map.of("message", "Status prijave je ažuriran."));
    }

    // Uredi podatke vlasnika prijave - samo za ADMINISTRATORA
    @PatchMapping("/prijave/{id}/vlasnik")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> updateVlasnik(
            @PathVariable("id") Long idPrijava,
            @Valid @RequestBody com.autoservis.interfaces.dto.OsobaUpdateDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Niste prijavljeni."));
        }

        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }

        prijavaService.updateVlasnik(idPrijava, dto, idOsoba);
        return ResponseEntity.ok(Map.of("message", "Podaci vlasnika su ažurirani."));
    }

    // ADMIN: dohvat prijava za određenog korisnika
    @GetMapping("/prijave/user")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> getPrijaveForUser(@RequestParam Long userId) {
        return ResponseEntity.ok(prijavaService.getPrijaveForKorisnik(userId));
    }

    // ADMIN: kreiraj prijavu za određenog korisnika
    @PostMapping("/prijave/admin")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> createPrijavaForUser(@Valid @RequestBody PrijavaServisaCreateDto dto, @RequestParam Long ownerId) {
        var created = prijavaService.createPrijavaForUser(dto, ownerId);
        return ResponseEntity.status(201).body(created);
    }

    // Delete prijava - owner (korisnik) or administrator
    @DeleteMapping("/prijave/{id}")
    public ResponseEntity<?> deletePrijava(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Niste prijavljeni."));
        }
        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }
        boolean isAdmin = "administrator".equalsIgnoreCase((String) jwt.getClaim("uloga"));
        prijavaService.deletePrijava(id, idOsoba, isAdmin);
        return ResponseEntity.ok(Map.of("message", "Prijava obrisana."));
    }
    
    // Dodaj napomenu na prijavu - samo za SERVISERE
    @PostMapping("/prijave/{id}/napomene")
    @PreAuthorize("hasAnyRole('SERVISER', 'ADMINISTRATOR')")
    public ResponseEntity<?> addNapomena(
            @PathVariable("id") Long idPrijava,
            @Valid @RequestBody NapomenaCreateDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Niste prijavljeni."));
        }
        
        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }
        
        prijavaService.addNapomena(idPrijava, dto, idOsoba);
        return ResponseEntity.status(201).body(Map.of("message", "Napomena je dodana."));
    }

    public static class UpdateVoziloDto { public Long idVozilo; }

    @PatchMapping("/prijave/{id}/vozilo")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> updateVozilo(
            @PathVariable("id") Long idPrijava,
            @RequestBody UpdateVoziloDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Niste prijavljeni."));
        }

        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }

        prijavaService.updateVozilo(idPrijava, dto.idVozilo, idOsoba);
        return ResponseEntity.ok(Map.of("message", "Vozilo je ažurirano."));
    }
}
