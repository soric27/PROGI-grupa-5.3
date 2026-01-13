package com.autoservis.interfaces.http;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoservis.interfaces.dto.OsobaDto;
import com.autoservis.repositories.OsobaRepository;

@RestController
@RequestMapping("/api/users")
public class OsobaController {
    private final OsobaRepository osobe;

    public OsobaController(OsobaRepository osobe) {
        this.osobe = osobe;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<OsobaDto>> getAll() {
        return ResponseEntity.ok(osobe.findAll().stream()
            .map(o -> new OsobaDto(o.getIdOsoba(), o.getIme() + " " + o.getPrezime(), o.getEmail()))
            .toList());
    }
}
