package com.autoservis.interfaces.http;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoservis.models.Marka;
import com.autoservis.models.Model;
import com.autoservis.models.Osoba;
import com.autoservis.models.Serviser;
import com.autoservis.models.Termin;
import com.autoservis.models.Vozilo;
import com.autoservis.repositories.MarkaRepository;
import com.autoservis.repositories.ModelRepository;
import com.autoservis.repositories.OsobaRepository;
import com.autoservis.repositories.ServiserRepository;
import com.autoservis.repositories.TerminRepository;
import com.autoservis.repositories.VoziloRepository;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    private final OsobaRepository osobe;
    private final ServiserRepository serviseri;
    private final TerminRepository termini;
    private final VoziloRepository vozila;
    private final MarkaRepository marke;
    private final ModelRepository modeli;

    public DebugController(OsobaRepository osobe, ServiserRepository serviseri, TerminRepository termini, VoziloRepository vozila, MarkaRepository marke, ModelRepository modeli) {
        this.osobe = osobe;
        this.serviseri = serviseri;
        this.termini = termini;
        this.vozila = vozila;
        this.marke = marke;
        this.modeli = modeli;
    }

    // Create a few sample servisers + terms + demo vehicle for a test user.
    // Protected: only administrators can run it.
    @PostMapping("/seed-test")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<?> seedTest() {
        // Create sample persons
        Osoba ivan = osobe.findByEmail("ivan.serviser@example.com").orElseGet(() -> osobe.save(new Osoba("Ivan", "Ivić", "ivan.serviser@example.com", "serviser", "srv-ivan")));
        Osoba marko = osobe.findByEmail("marko.serviser@example.com").orElseGet(() -> osobe.save(new Osoba("Marko", "Marković", "marko.serviser@example.com", "serviser", "srv-marko")));
        Osoba ana = osobe.findByEmail("ana.serviser@example.com").orElseGet(() -> osobe.save(new Osoba("Ana", "Anić", "ana.serviser@example.com", "serviser", "srv-ana")));

        // Ensure serviser rows
        Serviser s1 = serviseri.findByOsoba_IdOsoba(ivan.getIdOsoba()).orElseGet(() -> serviseri.save(new Serviser(ivan, false)));
        Serviser s2 = serviseri.findByOsoba_IdOsoba(marko.getIdOsoba()).orElseGet(() -> serviseri.save(new Serviser(marko, false)));
        Serviser s3 = serviseri.findByOsoba_IdOsoba(ana.getIdOsoba()).orElseGet(() -> serviseri.save(new Serviser(ana, false)));

        // Ensure there's at least one model to use for vehicles
        Model model = modeli.findAll().stream().findFirst().orElseGet(() -> {
            Marka m = marke.findAll().stream().findFirst().orElseGet(() -> marke.save(new Marka("Generic")));
            return modeli.save(new Model("Default", m));
        });

        // Create demo vehicle for the first non-serviser user (if any)
        var maybeUser = osobe.findByEmail("vitkovicdomagoj@gmail.com");
        if (maybeUser.isPresent()) {
            Osoba user = maybeUser.get();
            // create vehicle if none exists for that user
            List<Vozilo> userVeh = vozila.findAll().stream().filter(v -> v.getOsoba() != null && v.getOsoba().getIdOsoba().equals(user.getIdOsoba())).toList();
            if (userVeh.isEmpty()) {
                Vozilo v = new Vozilo(user, model, "TEST-123", 2010);
                vozila.save(v);
            }
        }

        // Create terms for next 7 days 9..16 for these servisers
        int created = 0;
        LocalDate today = LocalDate.now();
        for (Serviser s : List.of(s1, s2, s3)) {
            for (int i = 0; i <= 6; i++) {
                LocalDate d = today.plusDays(i);
                for (int h = 9; h <= 16; h++) {
                    LocalDateTime dt = LocalDateTime.of(d, LocalTime.of(h, 0));
                    boolean exists = termini.findAll().stream().anyMatch(t -> t.getDatumVrijeme().equals(dt) && t.getServiser() != null && t.getServiser().getIdServiser().equals(s.getIdServiser()));
                    if (!exists) {
                        // use constructor that sets serviser
                        Termin t = new Termin(dt, s);
                        termini.save(t);
                        created++;
                    }
                }
            }
        }

        return ResponseEntity.ok(Map.of("message", "Seed complete", "servisers", List.of(s1.getIdServiser(), s2.getIdServiser(), s3.getIdServiser()), "created_terms", created));
    }
}
