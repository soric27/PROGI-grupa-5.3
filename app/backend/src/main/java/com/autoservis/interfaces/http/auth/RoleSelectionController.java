package com.autoservis.interfaces.http.auth;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class RoleSelectionController {

    private final OsobaRepository osobaRepository;
    private final ServiserRepository serviserRepository;
    private final JwtTokenProvider jwtProvider;

    private final Logger log = LoggerFactory.getLogger(RoleSelectionController.class);

    public RoleSelectionController(OsobaRepository osobaRepository, ServiserRepository serviserRepository, JwtTokenProvider jwtProvider) {
        this.osobaRepository = osobaRepository;
        this.serviserRepository = serviserRepository;
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

        Osoba osoba = osobaRepository.findById(idOsoba)
            .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        // Validiraj ulogu
        String novaUloga = request.uloga().toLowerCase();
        if (!novaUloga.matches("korisnik|serviser|administrator")) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Nevažeća uloga. Dozvoljene su: korisnik, serviser, administrator")
            );
        }

        // Ažuriraj ulogu (ažuriramo postojeću osobu, ne kreiramo novu)
        osoba.setUloga(novaUloga);
        osoba = osobaRepository.save(osoba);

        // Ako je odabrana uloga "serviser", osiguraj da postoji zapis u tablici serviser
        if ("serviser".equals(novaUloga)) {
            try {
                serviserRepository.findByOsoba_IdOsoba(idOsoba).orElseGet(() -> {
                    Serviser s = new Serviser(osoba, false);
                    return serviserRepository.save(s);
                });
            } catch (Exception ex) {
                log.error("Neuspjelo kreiranje serviser-a za id {}: {}", idOsoba, ex.toString());
                return ResponseEntity.status(500).body(new ErrorResponse("Pogreška pri stvaranju servisera. Kontaktirajte administratora."));
            }
        }

        // Generiraj novi token sa novom ulogom
        String noviToken = jwtProvider.generateToken(osoba);

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
