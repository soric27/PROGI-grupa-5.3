package com.autoservis.interfaces.http;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoservis.interfaces.dto.OsobaDto;
import com.autoservis.interfaces.dto.OsobaUpsertDto;
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
            .map(o -> new OsobaDto(o.getIdOsoba(), o.getIme() + " " + o.getPrezime(), o.getEmail(), o.getUloga()))
            .toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> create(@RequestBody OsobaUpsertDto dto) {
        if (osobe.findByEmail(dto.email()).isPresent()) return ResponseEntity.badRequest().body("Email already in use");
        com.autoservis.models.Osoba o = new com.autoservis.models.Osoba(dto.ime(), dto.prezime(), dto.email(), dto.uloga(), null);
        o = osobe.save(o);
        return ResponseEntity.ok(new OsobaDto(o.getIdOsoba(), o.getIme() + " " + o.getPrezime(), o.getEmail(), o.getUloga()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody OsobaUpsertDto dto) {
        var opt = osobe.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var o = opt.get();
        o.setIme(dto.ime()); o.setPrezime(dto.prezime()); o.setEmail(dto.email()); o.setUloga(dto.uloga());
        o = osobe.save(o);
        return ResponseEntity.ok(new OsobaDto(o.getIdOsoba(), o.getIme() + " " + o.getPrezime(), o.getEmail(), o.getUloga()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!osobe.existsById(id)) return ResponseEntity.notFound().build();
        osobe.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
