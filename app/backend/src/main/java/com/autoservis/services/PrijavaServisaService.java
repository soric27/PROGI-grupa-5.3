package com.autoservis.services;

import com.autoservis.interfaces.dto.PrijavaServisaCreateDto;
import com.autoservis.models.*;
import com.autoservis.repositories.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrijavaServisaService {

    private final PrijavaServisaRepository prijave;
    private final VoziloRepository vozila;
    private final ServiserRepository serviseri;
    private final TerminRepository termini;

    public PrijavaServisaService(PrijavaServisaRepository prijave, VoziloRepository vozila, ServiserRepository serviseri, TerminRepository termini) {
        this.prijave = prijave;
        this.vozila = vozila;
        this.serviseri = serviseri;
        this.termini = termini;
    }

    @Transactional
    public void createPrijava(PrijavaServisaCreateDto dto, Long idVlasnika) {
        Vozilo vozilo = vozila.findById(dto.idVozilo())
                .orElseThrow(() -> new IllegalArgumentException("Vozilo ne postoji."));

        // SIGURNOSNA PROVJERA: Osiguraj da korisnik prijavljuje SVOJE vozilo
        if (!vozilo.getOsoba().getIdOsoba().equals(idVlasnika)) {
            throw new AccessDeniedException("Nemate pravo prijaviti ovo vozilo.");
        }

        Serviser serviser = serviseri.findById(dto.idServiser())
                .orElseThrow(() -> new IllegalArgumentException("Serviser ne postoji."));

        Termin termin = termini.findById(dto.idTermin())
                .orElseThrow(() -> new IllegalArgumentException("Termin ne postoji."));

        if (termin.isZauzet()) {
            throw new IllegalStateException("Odabrani termin je u međuvremenu zauzet.");
        }

        // Označi termin kao zauzet i spremi promjenu
        termin.setZauzet(true);
        termini.save(termin);

        // Kreiraj i spremi novu prijavu
        PrijavaServisa novaPrijava = new PrijavaServisa(
                vozilo, serviser, termin, dto.napomenaVlasnika()
        );
        prijave.save(novaPrijava);
        
        // Ovdje bi se mogla vratiti DTO verzija spremljene prijave ako je potrebno
    }
}