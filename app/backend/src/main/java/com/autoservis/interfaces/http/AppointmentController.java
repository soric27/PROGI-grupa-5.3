package com.autoservis.interfaces.http;

import com.autoservis.interfaces.dto.PrijavaServisaCreateDto;
import com.autoservis.interfaces.dto.ServiserDto;
import com.autoservis.interfaces.dto.TerminDto;
import com.autoservis.services.PrijavaServisaService;
import com.autoservis.services.ServiserService;
import com.autoservis.services.TerminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // Endpoint za dohvaćanje slobodnih termina
    @GetMapping("/termini")
    public ResponseEntity<List<TerminDto>> getSlobodniTermini() {
        return ResponseEntity.ok(terminService.getSlobodniTermini());
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
        Long idVlasnika = jwt.getClaim("id_osoba");
        if (idVlasnika == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Nevažeći token."));
        }
        
        prijavaService.createPrijava(dto, idVlasnika);
        
        return ResponseEntity.status(201).body(Map.of("message", "Prijava za servis je uspješno kreirana."));
    }
}