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

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    public OAuth2SuccessHandler(JwtEncoder encoder, OsobaRepository osobaRepository) {
        this.encoder = encoder;
        this.osobaRepository = osobaRepository;
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
        Osoba osoba = osobaRepository.findByEmail(email)
            .orElseGet(() -> {
                Osoba novaOsoba = new Osoba(ime, prez, email, "korisnik", oauthId);
                return osobaRepository.save(novaOsoba);
            });

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
        String redirect = frontendUrl + "/role-selection?token=" + token;
        try {
            response.sendRedirect(redirect);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
