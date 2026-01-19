package com.autoservis.interfaces.http.auth;

import com.autoservis.models.Osoba;
import com.autoservis.repositories.OsobaRepository;
import com.autoservis.security.JwtTokenProvider;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class RoleSelectionController {

    private final OsobaRepository osobaRepository;
    private final JwtTokenProvider jwtProvider;

    public RoleSelectionController(OsobaRepository osobaRepository, JwtTokenProvider jwtProvider) {
        this.osobaRepository = osobaRepository;
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

        // Dohvati korisnika iz baze
        Long idOsoba = jwt.getClaim("id_osoba");
        if (idOsoba == null) {
            return ResponseEntity.status(401).body(
                new ErrorResponse("Nevažeći token. Prijavite se ponovno.")
            );
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

        // Ažuriraj ulogu u bazi
        osoba = new Osoba(
            osoba.getIme(),
            osoba.getPrezime(),
            osoba.getEmail(),
            novaUloga,
            osoba.getOauthId()
        );
        osoba = osobaRepository.save(osoba);

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
