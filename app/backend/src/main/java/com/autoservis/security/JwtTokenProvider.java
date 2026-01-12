package com.autoservis.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.autoservis.models.Osoba;

@Component
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt-expiration:3600}")
    private long jwtExpiration;

    public JwtTokenProvider(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(Osoba osoba) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(osoba.getIdOsoba().toString())
            .claim("id_osoba", osoba.getIdOsoba())
            .claim("email", osoba.getEmail())
            .claim("ime", osoba.getIme())
            .claim("prezime", osoba.getPrezime())
            .claim("uloga", osoba.getUloga())
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();  // ← PROMIJENI OVU LINIJU
    }
}
