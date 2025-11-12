package com.autoservis.security;

import java.time.Instant;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final JwtEncoder encoder;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  public OAuth2SuccessHandler(JwtEncoder encoder) {
    this.encoder = encoder;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) {
    OAuth2User user = (OAuth2User) authentication.getPrincipal();

    String email = (String) user.getAttributes().getOrDefault("email", "unknown");
    String ime   = (String) user.getAttributes().getOrDefault("given_name", "");
    String prez  = (String) user.getAttributes().getOrDefault("family_name", "");

    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .subject(email)
        .issuedAt(now)
        .expiresAt(now.plusSeconds(3600)) // 1h
        .claim("ime", ime)
        .claim("prezime", prez)
        .claim("email", email)
        .build();

    String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

    // Pošalji token natrag frontendu – preko hash-a da ga SPA lako pročita
    String redirect = frontendUrl + "/#token=" + token;
    try {
      response.sendRedirect(redirect);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}