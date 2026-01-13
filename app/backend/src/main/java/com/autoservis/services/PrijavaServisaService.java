package com.autoservis.services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoservis.interfaces.dto.NapomenaCreateDto;
import com.autoservis.interfaces.dto.PrijavaDetalleDto;
import com.autoservis.interfaces.dto.PrijavaServisaCreateDto;
import com.autoservis.models.NapomenaServisera;
import com.autoservis.models.PrijavaServisa;
import com.autoservis.models.Serviser;           // ← DODAJ OVU LINIJU
import com.autoservis.models.Termin;
import com.autoservis.models.Vozilo;
import com.autoservis.repositories.NapomenaServiseraRepository;
import com.autoservis.repositories.OsobaRepository;
import com.autoservis.repositories.PrijavaServisaRepository;
import com.autoservis.repositories.ServiserRepository;
import com.autoservis.repositories.TerminRepository;
import com.autoservis.repositories.VoziloRepository;
import com.autoservis.shared.PrijavaServisaMapper; 


@Service
public class PrijavaServisaService {

    private final PrijavaServisaRepository prijave;
    private final VoziloRepository vozila;
    private final ServiserRepository serviseri;
    private final TerminRepository termini;
    private final NapomenaServiseraRepository napomeneRepo;
    private final OsobaRepository osobeRepo;

    public PrijavaServisaService(PrijavaServisaRepository prijave, VoziloRepository vozila, ServiserRepository serviseri, TerminRepository termini, NapomenaServiseraRepository n, OsobaRepository osobeRepo) {
        this.prijave = prijave;
        this.vozila = vozila;
        this.serviseri = serviseri;
        this.termini = termini;
        this.napomeneRepo = n;
        this.osobeRepo = osobeRepo;
    }

    @Transactional
    public void updateVlasnik(Long idPrijava, com.autoservis.interfaces.dto.OsobaUpdateDto dto, Long idOsobaPrincipal) {
        PrijavaServisa prijava = prijave.findById(idPrijava)
                .orElseThrow(() -> new IllegalArgumentException("Prijava ne postoji."));

        // SIGURNOSNA PROVJERA: samo serviser dodijeljen prijavi (ili voditelj) može uređivati podatke vlasnika
        Serviser serviser = serviseri.findByOsoba_IdOsoba(idOsobaPrincipal)
                .orElseThrow(() -> new AccessDeniedException("Nemate ovlasti za ovu akciju."));

        if (!prijava.getServiser().getIdServiser().equals(serviser.getIdServiser()) && !serviser.isJeLiVoditelj()) {
            throw new AccessDeniedException("Možete uređivati podatke samo kod svojih prijava.");
        }

        // Dohvati vlasnika vozila i ažuriraj polja ako su proslijeđena
        com.autoservis.models.Osoba vlasnik = prijava.getVozilo().getOsoba();
        if (dto.ime() != null && !dto.ime().isBlank()) vlasnik.setIme(dto.ime());
        if (dto.prezime() != null && !dto.prezime().isBlank()) vlasnik.setPrezime(dto.prezime());
        if (dto.email() != null && !dto.email().isBlank()) vlasnik.setEmail(dto.email());

        osobeRepo.save(vlasnik);
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
    }

    @Transactional(readOnly = true)
    public List<PrijavaDetalleDto> getPrijaveForKorisnik(Long idOsoba) {
        return prijave.findAll().stream()
                .filter(p -> p.getVozilo().getOsoba().getIdOsoba().equals(idOsoba))
                .map(PrijavaServisaMapper::toDetailDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrijavaDetalleDto> getPrijaveForServiser(Long idOsoba) {
        Serviser serviser = serviseri.findByOsoba_IdOsoba(idOsoba)
                .orElseThrow(() -> new AccessDeniedException("Osoba nije serviser."));
        
        return prijave.findAll().stream()
                .filter(p -> p.getServiser().getIdServiser().equals(serviser.getIdServiser()))
                .map(PrijavaServisaMapper::toDetailDto)
                .toList();
    }

    @Transactional
    public void updateStatus(Long idPrijava, String noviStatus, Long idOsobaPrincipal) {
        PrijavaServisa prijava = prijave.findById(idPrijava)
                .orElseThrow(() -> new IllegalArgumentException("Prijava ne postoji."));

        // SIGURNOSNA PROVJERA
        Serviser serviser = serviseri.findByOsoba_IdOsoba(idOsobaPrincipal)
                .orElseThrow(() -> new AccessDeniedException("Nemate ovlasti za ovu akciju."));
        
        if (!prijava.getServiser().getIdServiser().equals(serviser.getIdServiser()) && !serviser.isJeLiVoditelj()) {
            throw new AccessDeniedException("Možete mijenjati status samo svojih prijava.");
        }

        prijava.setStatus(noviStatus);
        prijave.save(prijava);
    }
    
    @Transactional
    public void addNapomena(Long idPrijava, NapomenaCreateDto dto, Long idOsobaPrincipal) {
        PrijavaServisa prijava = prijave.findById(idPrijava)
                .orElseThrow(() -> new IllegalArgumentException("Prijava ne postoji."));
         
        // SIGURNOSNA PROVJERA
        Serviser serviser = serviseri.findByOsoba_IdOsoba(idOsobaPrincipal)
                .orElseThrow(() -> new AccessDeniedException("Nemate ovlasti za ovu akciju."));
        
        if (!prijava.getServiser().getIdServiser().equals(serviser.getIdServiser()) && !serviser.isJeLiVoditelj()) {
            throw new AccessDeniedException("Možete dodavati napomene samo na svoje prijave.");
        }
        
        NapomenaServisera novaNapomena = new NapomenaServisera(prijava, dto.opis());
        napomeneRepo.save(novaNapomena);
    }
}
