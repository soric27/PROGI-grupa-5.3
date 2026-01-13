package com.autoservis.interfaces.http.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoservis.models.Osoba;
import com.autoservis.models.Serviser;
import com.autoservis.repositories.OsobaRepository;
import com.autoservis.repositories.ServiserRepository;
import com.autoservis.security.JwtTokenProvider;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class RoleSelectionController {

    private final OsobaRepository osobaRepository;
    private final ServiserRepository serviserRepository;
    private final com.autoservis.repositories.TerminRepository terminRepository;
    private final JwtTokenProvider jwtProvider;

    private final Logger log = LoggerFactory.getLogger(RoleSelectionController.class);

    public RoleSelectionController(OsobaRepository osobaRepository, ServiserRepository serviserRepository, com.autoservis.repositories.TerminRepository terminRepository, JwtTokenProvider jwtProvider) {
        this.osobaRepository = osobaRepository;
        this.serviserRepository = serviserRepository;
        this.terminRepository = terminRepository;
        this.jwtProvider = jwtProvider;
    }

    // POST /api/auth/select-role
    @PostMapping("/select-role")
    public ResponseEntity<?> selectRole(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody RoleSelectionRequest request
    ) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(
                new ErrorResponse("Nevažeći token. Prijavite se ponovno.")
            );
        }

        // Dohvati korisnika iz baze (sigurno parsiranje id_osoba)
        Long idOsoba;
        try {
            Object claim = jwt.getClaim("id_osoba");
            if (claim instanceof Number) {
                idOsoba = ((Number) claim).longValue();
            } else if (claim instanceof String) {
                idOsoba = Long.parseLong((String) claim);
            } else {
                log.warn("Nevažeći tip za claim id_osoba: {}", claim == null ? "null" : claim.getClass().getName());
                return ResponseEntity.status(400).body(new ErrorResponse("Nevažeći token (id_osoba)."));
            }
        } catch (Exception ex) {
            log.error("Greška pri parsiranju id_osoba iz JWT: {}", ex.toString());
            return ResponseEntity.status(400).body(new ErrorResponse("Nevažeći token (id_osoba)."));
        }

        var osobaOpt = osobaRepository.findById(idOsoba);
        if (osobaOpt.isEmpty()) {
            log.warn("Korisnik s id {} nije pronađen", idOsoba);
            return ResponseEntity.status(404).body(new ErrorResponse("Korisnik nije pronađen."));
        }
        Osoba osoba = osobaOpt.get();

        // Validiraj ulogu
        String novaUloga = request.uloga() == null ? "" : request.uloga().toLowerCase();
        if (!novaUloga.matches("korisnik|serviser|administrator")) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Nevažeća uloga. Dozvoljene su: korisnik, serviser, administrator")
            );
        }

        // Ažuriraj ulogu (ažuriramo postojeću osobu, ne kreiramo novu)
        osoba.setUloga(novaUloga);
        try {
            osoba = osobaRepository.save(osoba);
        } catch (Exception ex) {
            log.error("Greška pri spremanju uloge za osobu id {}: {}", idOsoba, ex.toString());
            return ResponseEntity.status(500).body(new ErrorResponse("Pogreška pri ažuriranju uloge. Kontaktirajte administratora."));
        }
        final Osoba savedOsoba = osoba; // ensure effectively final for lambda

        // Ako je odabrana uloga "serviser", osiguraj da postoji zapis u tablici serviser i generiraj termine ako ih nema
        if ("serviser".equals(novaUloga)) {
            try {
                Serviser s = serviserRepository.findByOsoba_IdOsoba(idOsoba).orElseGet(() -> {
                    Serviser svc = new Serviser(savedOsoba, false);
                    return serviserRepository.save(svc);
                });

                // Ako serviser nema termine, dodaj osnovne termine (npr. sljedećih 7 dana, 9:00-16:00)
                var existing = terminRepository.findByServiser_IdServiser(s.getIdServiser());
                if (existing == null || existing.isEmpty()) {
                    java.time.LocalDate today = java.time.LocalDate.now();
                    java.util.List<com.autoservis.models.Termin> slots = new java.util.ArrayList<>();
                    for (int d = 0; d < 7; d++) {
                        for (int h = 9; h < 17; h++) {
                            java.time.LocalDateTime dt = today.plusDays(d).atTime(h, 0);
                            slots.add(new com.autoservis.models.Termin(dt, s));
                        }
                    }
                    terminRepository.saveAll(slots);
                }

            } catch (Exception ex) {
                log.error("Neuspjelo kreiranje serviser-a za id {}: {}", idOsoba, ex.toString());
                return ResponseEntity.status(500).body(new ErrorResponse("Pogreška pri stvaranju servisera. Kontaktirajte administratora."));
            }
        }

        // Generiraj novi token sa novom ulogom
        String noviToken;
        try {
            noviToken = jwtProvider.generateToken(osoba);
        } catch (Exception ex) {
            log.error("Greška pri generiranju tokena za osobu id {}: {}", idOsoba, ex.toString());
            return ResponseEntity.status(500).body(new ErrorResponse("Pogreška pri generiranju tokena. Kontaktirajte administratora."));
        }

        return ResponseEntity.ok(new RoleSelectionResponse(noviToken, "Uloga je uspješno odabrana."));
    }

    // DTOs
    public record RoleSelectionRequest(
        @NotBlank String uloga
    ) {}

    public record RoleSelectionResponse(
        String token,
        String message
    ) {}

    public record ErrorResponse(String message) {}
}
