package com.autoservis.interfaces.http.auth;

import com.autoservis.security.SpringOsobaPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {

  // SPA entry – SecurityConfig preuzima stvarni redirect
  @GetMapping("/login/google")
  public ResponseEntity<?> login() { return ResponseEntity.ok().build(); }

  @GetMapping("/user")
  public ResponseEntity<?> currentUser(Authentication auth){
    if (auth == null || !(auth.getPrincipal() instanceof SpringOsobaPrincipal p))
      return ResponseEntity.status(401).body(new Message("Niste prijavljeni"));
    var o = p.getOsoba();
    return ResponseEntity.ok(new OsobaView(o.getIdOsoba(), o.getIme(), o.getPrezime(), o.getEmail(), o.getUloga()));
  }

  public record OsobaView(Long id_osoba, String ime, String prezime, String email, String uloga) {}
  public record Message(String message) {}
}