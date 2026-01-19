package com.autoservis.interfaces.http.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  // Endpoint koji vraća podatke o trenutnom korisniku na temelju JWT tokena
  @GetMapping("/user")
  public ResponseEntity<?> currentUser(@AuthenticationPrincipal Jwt jwt) {
    if (jwt == null) {
      return ResponseEntity.status(401).body(new Message("Niste prijavljeni"));
    }

    // Polja iz JWT-a (claimovi)
    String ime = jwt.getClaimAsString("ime");
    String prezime = jwt.getClaimAsString("prezime");
    String email = jwt.getClaimAsString("email");

    return ResponseEntity.ok(new OsobaView(null, ime, prezime, email, "KORISNIK"));
  }

  public record OsobaView(Long id_osoba, String ime, String prezime, String email, String uloga) {}
  public record Message(String message) {}
}
