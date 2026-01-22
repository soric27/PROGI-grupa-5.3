package com.autoservis.security;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.autoservis.models.Osoba;
import com.autoservis.repositories.OsobaRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtEncoder encoder;
    private final OsobaRepository osobaRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.admin-emails:}")
    private String adminEmails;

    public OAuth2SuccessHandler(JwtEncoder encoder, OsobaRepository osobaRepository) {
        this.encoder = encoder;
        this.osobaRepository = osobaRepository;
    }

    private boolean isBootstrapAdmin(String email) {
        if (email == null || email.isBlank() || adminEmails == null || adminEmails.isBlank()) return false;
        for (String e : adminEmails.split(",")) {
            if (email.equalsIgnoreCase(e.trim())) return true;
        }
        return false;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        String email = (String) user.getAttributes().getOrDefault("email", "unknown");
        String ime = (String) user.getAttributes().getOrDefault("given_name", "");
        String prez = (String) user.getAttributes().getOrDefault("family_name", "");
        String oauthId = (String) user.getAttributes().getOrDefault("sub", "");

        // Dohvati ili kreiraj osobu
        boolean shouldBeAdmin = isBootstrapAdmin(email);
        Osoba osoba = osobaRepository.findByEmail(email)
            .orElseGet(() -> {
                String role = shouldBeAdmin ? "administrator" : "korisnik";
                Osoba novaOsoba = new Osoba(ime, prez, email, role, oauthId);
                return osobaRepository.save(novaOsoba);
            });
        if (shouldBeAdmin && !"administrator".equalsIgnoreCase(osoba.getUloga())) {
            osoba.setUloga("administrator");
            osoba = osobaRepository.save(osoba);
        }

        Long idOsoba = osoba.getIdOsoba();

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(email)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .claim("ime", ime)
            .claim("prezime", prez)
            .claim("email", email)
            .claim("id_osoba", idOsoba)
            .claim("uloga", osoba.getUloga())
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        // Pošalji korisnika na stranicu za izbor uloge tako da može odmah odabrati serviser/korisnik
        String redirect = frontendUrl + "/?token=" + token;
        try {
            response.sendRedirect(redirect);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
