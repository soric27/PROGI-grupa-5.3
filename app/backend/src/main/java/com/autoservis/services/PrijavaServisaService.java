package com.autoservis.services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoservis.interfaces.dto.NapomenaCreateDto;
import com.autoservis.interfaces.dto.PrijavaDetalleDto;
import com.autoservis.interfaces.dto.PrijavaServisaCreateDto;
import com.autoservis.models.Kvar;
import com.autoservis.models.NapomenaServisera;
import com.autoservis.models.PrijavaServisa;
import com.autoservis.models.Serviser;
import com.autoservis.models.Termin;
import com.autoservis.models.Vozilo;
import com.autoservis.repositories.KvarRepository;
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
    private final ZamjenaService zamjenaService;
    private final KvarRepository kvarRepository;
    private final PdfService pdfService;
    private final EmailService emailService;

    public PrijavaServisaService(PrijavaServisaRepository prijave, VoziloRepository vozila, ServiserRepository serviseri, TerminRepository termini, NapomenaServiseraRepository n, OsobaRepository osobeRepo, ZamjenaService zamjenaService, KvarRepository kvarRepository, PdfService pdfService, EmailService emailService) {
        this.prijave = prijave;
        this.vozila = vozila;
        this.serviseri = serviseri;
        this.termini = termini;
        this.napomeneRepo = n;
        this.osobeRepo = osobeRepo;
        this.zamjenaService = zamjenaService;
        this.kvarRepository = kvarRepository;
        this.pdfService = pdfService;
        this.emailService = emailService;
    }

    @Transactional
    public void updateVlasnik(Long idPrijava, com.autoservis.interfaces.dto.OsobaUpdateDto dto, Long idOsobaPrincipal) {
        PrijavaServisa prijava = prijave.findById(idPrijava)
                .orElseThrow(() -> new IllegalArgumentException("Prijava ne postoji."));

        // Only administrators should call this method (controller enforces it)
        // Dohvati vlasnika vozila i ažuriraj polja ako su proslijeđena
        com.autoservis.models.Osoba vlasnik = prijava.getVozilo().getOsoba();
        if (dto.ime() != null && !dto.ime().isBlank()) vlasnik.setIme(dto.ime());
        if (dto.prezime() != null && !dto.prezime().isBlank()) vlasnik.setPrezime(dto.prezime());
        if (dto.email() != null && !dto.email().isBlank()) vlasnik.setEmail(dto.email());

        osobeRepo.save(vlasnik);
    }

    @Transactional
    public com.autoservis.interfaces.dto.PrijavaDetalleDto createPrijava(PrijavaServisaCreateDto dto, Long idVlasnika) {
        Vozilo vozilo = vozila.findById(dto.idVozilo())
                .orElseThrow(() -> new IllegalArgumentException("Vozilo ne postoji."));

        // Prevent duplicate active prijave for the same vehicle
        var existingForVehicle = prijave.findByVozilo_IdVozilo(dto.idVozilo());
        boolean hasActive = existingForVehicle.stream()
                .anyMatch(p -> {
                    String status = p.getStatus() == null ? "" : p.getStatus().toLowerCase();
                    return !status.contains("zavr");
                });
        if (hasActive) {
            throw new IllegalStateException("Vozilo već ima aktivnu prijavu.");
        }

        // SIGURNOSNA PROVJERA: Osiguraj da korisnik prijavljuje SVOJE vozilo
        if (!vozilo.getOsoba().getIdOsoba().equals(idVlasnika)) {
            throw new AccessDeniedException("Nemate pravo prijaviti ovo vozilo.");
        }

        Serviser serviser = serviseri.findById(dto.idServiser())
                .orElseThrow(() -> new IllegalArgumentException("Serviser ne postoji."));

        Termin termin = termini.findById(dto.idTermin())
                .orElseThrow(() -> new IllegalArgumentException("Termin ne postoji."));

        // Provjeri da termin pripada odabranom serviseru
        if (termin.getServiser() == null || !termin.getServiser().getIdServiser().equals(dto.idServiser())) {
            throw new IllegalArgumentException("Odabrani termin ne pripada odabranom serviseru.");
        }

        // Pokušaj atomskog markiranja termina kao zauzetog
        int updated = termini.markAsTaken(termin.getIdTermin());
        if (updated == 0) {
            throw new IllegalStateException("Odabrani termin je u međuvremenu zauzet.");
        }

        // Kreiraj i spremi novu prijavu
        PrijavaServisa novaPrijava = new PrijavaServisa(
                vozilo, serviser, termin, dto.napomenaVlasnika()
        );
        
        // Dodaj odabrane kvarove ako su proslijeđeni
        if (dto.idKvarovi() == null || dto.idKvarovi().isEmpty()) {
            throw new IllegalArgumentException("Potrebno je odabrati barem jedan kvar.");
        }

        if (dto.idKvarovi() != null && !dto.idKvarovi().isEmpty()) {
            java.util.List<Kvar> kvarovi = kvarRepository.findAllById(dto.idKvarovi());
            novaPrijava.setKvarovi(kvarovi);
        }
        
        prijave.save(novaPrijava);

        // If user requested a replacement vehicle at creation, try to reserve it
        com.autoservis.models.RezervacijaZamjene rez = null;
        if (dto.idZamjena() != null && dto.datumOd() != null && dto.datumDo() != null) {
            rez = zamjenaService.reserve(novaPrijava.getIdPrijava(), dto.idZamjena(), dto.datumOd(), dto.datumDo());
        }

        sendPrijavaEmail(novaPrijava);

        return com.autoservis.shared.PrijavaServisaMapper.toDetailDto(novaPrijava, rez);
    }

    // Administrator: kreiraj prijavu za bilo kojeg korisnika (provjera vlasništva vozila)
    @Transactional
    public com.autoservis.interfaces.dto.PrijavaDetalleDto createPrijavaForUser(PrijavaServisaCreateDto dto, Long idVlasnika) {
        Vozilo vozilo = vozila.findById(dto.idVozilo())
                .orElseThrow(() -> new IllegalArgumentException("Vozilo ne postoji."));

        var existingForVehicle = prijave.findByVozilo_IdVozilo(dto.idVozilo());
        boolean hasActive = existingForVehicle.stream()
                .anyMatch(p -> {
                    String status = p.getStatus() == null ? "" : p.getStatus().toLowerCase();
                    return !status.contains("zavr");
                });
        if (hasActive) {
            throw new IllegalStateException("Vozilo već ima aktivnu prijavu.");
        }

        // Provjeri da vozilo pripada odabranoj osobi
        if (!vozilo.getOsoba().getIdOsoba().equals(idVlasnika)) {
            throw new IllegalArgumentException("Odabrano vozilo ne pripada navedenom korisniku.");
        }

        Serviser serviser = serviseri.findById(dto.idServiser())
                .orElseThrow(() -> new IllegalArgumentException("Serviser ne postoji."));

        Termin termin = termini.findById(dto.idTermin())
                .orElseThrow(() -> new IllegalArgumentException("Termin ne postoji."));

        if (termin.getServiser() == null || !termin.getServiser().getIdServiser().equals(dto.idServiser())) {
            throw new IllegalArgumentException("Odabrani termin ne pripada odabranom serviseru.");
        }

        int updated = termini.markAsTaken(termin.getIdTermin());
        if (updated == 0) {
            throw new IllegalStateException("Odabrani termin je u međuvremenu zauzet.");
        }

        PrijavaServisa novaPrijava = new PrijavaServisa(
                vozilo, serviser, termin, dto.napomenaVlasnika()
        );
        
        // Dodaj odabrane kvarove ako su proslijeđeni
        if (dto.idKvarovi() == null || dto.idKvarovi().isEmpty()) {
            throw new IllegalArgumentException("Potrebno je odabrati barem jedan kvar.");
        }

        if (dto.idKvarovi() != null && !dto.idKvarovi().isEmpty()) {
            java.util.List<Kvar> kvarovi = kvarRepository.findAllById(dto.idKvarovi());
            novaPrijava.setKvarovi(kvarovi);
        }
        
        prijave.save(novaPrijava);

        com.autoservis.models.RezervacijaZamjene rez = null;
        if (dto.idZamjena() != null && dto.datumOd() != null && dto.datumDo() != null) {
            rez = zamjenaService.reserve(novaPrijava.getIdPrijava(), dto.idZamjena(), dto.datumOd(), dto.datumDo());
        }

        sendPrijavaEmail(novaPrijava);

        return com.autoservis.shared.PrijavaServisaMapper.toDetailDto(novaPrijava, rez);
    }

    // Administrator: obriši prijavu i oslobodi termin
    @Transactional
    public void deletePrijavaAsAdmin(Long idPrijave) {
        PrijavaServisa prijava = prijave.findById(idPrijave)
                .orElseThrow(() -> new IllegalArgumentException("Prijava ne postoji."));

        var rezervacije = zamjenaService.getReservationsForPrijava(prijava.getIdPrijava());
        for (var rez : rezervacije) {
            zamjenaService.returnReservation(rez.getIdRezervacija());
        }

        Termin termin = prijava.getTermin();
        if (termin != null) {
            termin.setZauzet(false);
            termini.save(termin);
        }

        prijave.deleteById(idPrijave);
    }

    // Delete prijava: allowed for owner (korisnik) or administrator
    @Transactional
    public void deletePrijava(Long idPrijave, Long idOsobaPrincipal, boolean isAdmin) {
        PrijavaServisa prijava = prijave.findById(idPrijave)
                .orElseThrow(() -> new IllegalArgumentException("Prijava ne postoji."));

        Long ownerId = prijava.getVozilo().getOsoba().getIdOsoba();
        if (!isAdmin && !ownerId.equals(idOsobaPrincipal)) {
            throw new org.springframework.security.access.AccessDeniedException("Nemate ovlasti za brisanje ove prijave.");
        }

        var rezervacije = zamjenaService.getReservationsForPrijava(prijava.getIdPrijava());
        for (var rez : rezervacije) {
            zamjenaService.returnReservation(rez.getIdRezervacija());
        }

        Termin termin = prijava.getTermin();
        if (termin != null) {
            termin.setZauzet(false);
            termini.save(termin);
        }

        prijave.deleteById(idPrijave);
    }

    @Transactional(readOnly = true)
    public List<PrijavaDetalleDto> getPrijaveForKorisnik(Long idOsoba) {
        return prijave.findAll().stream()
                .filter(p -> p.getVozilo().getOsoba().getIdOsoba().equals(idOsoba))
                .filter(p -> {
                    String status = p.getStatus() == null ? "" : p.getStatus().toLowerCase();
                    return !status.contains("zavr");
                })
                .map(p -> {
                    var rezList = zamjenaService.getReservationsForPrijava(p.getIdPrijava());
                    var latest = rezList.isEmpty() ? null : rezList.get(rezList.size()-1);
                    return PrijavaServisaMapper.toDetailDto(p, latest);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrijavaDetalleDto> getPrijaveForServiser(Long idOsoba) {
        // If the person is not registered as a serviser in DB, return empty list (caller may still be allowed by role)
        return serviseri.findByOsoba_IdOsoba(idOsoba)
                .map(serviser -> prijave.findAll().stream()
                        .filter(p -> p.getServiser().getIdServiser().equals(serviser.getIdServiser()))
                        .filter(p -> {
                            String status = p.getStatus() == null ? "" : p.getStatus().toLowerCase();
                            return !status.contains("zavr");
                        })
                        .map(p -> {
                            var rezList = zamjenaService.getReservationsForPrijava(p.getIdPrijava());
                            var latest = rezList.isEmpty() ? null : rezList.get(rezList.size()-1);
                            return PrijavaServisaMapper.toDetailDto(p, latest);
                        })
                        .toList()
                )
                .orElse(java.util.List.of());
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

        String normalized = noviStatus == null ? "" : noviStatus.trim().toLowerCase();
        if (normalized.contains("zavr")) {
            throw new IllegalArgumentException("Za zavrsetak koristite gumb 'Zavrsi servis'.");
        }

        prijava.setStatus(noviStatus);
        prijave.save(prijava);
    }

    @Transactional
    public void completePrijava(Long idPrijava, Long idOsobaPrincipal, boolean isAdmin) {
        PrijavaServisa prijava = prijave.findById(idPrijava)
                .orElseThrow(() -> new IllegalArgumentException("Prijava ne postoji."));

        if (!isAdmin) {
            Serviser serviser = serviseri.findByOsoba_IdOsoba(idOsobaPrincipal)
                    .orElseThrow(() -> new AccessDeniedException("Nemate ovlasti za ovu akciju."));
            if (!prijava.getServiser().getIdServiser().equals(serviser.getIdServiser()) && !serviser.isJeLiVoditelj()) {
                throw new AccessDeniedException("Mozete zavrsiti samo svoje prijave.");
            }
        }

        String to = null;
        if (prijava.getVozilo() != null && prijava.getVozilo().getOsoba() != null) {
            to = prijava.getVozilo().getOsoba().getEmail();
        }
        if (to != null && !to.isBlank()) {
            try {
                java.io.File pdf = pdfService.generateObrazacPdf(prijava, "preuzimanje");
                String subj = "Servis zavrsen - prijava " + prijava.getIdPrijava();
                String body = "Vas servis je zavrsen. U privitku je obrazac s podacima o servisu.";
                emailService.sendEmailWithAttachment(to, subj, body, pdf);
                deleteFileQuietly(pdf);
            } catch (Exception ex) {
                throw new IllegalStateException("Slanje emaila nije uspjelo.", ex);
            }
        }

        var rezervacije = zamjenaService.getReservationsForPrijava(prijava.getIdPrijava());
        for (var rez : rezervacije) {
            zamjenaService.returnReservation(rez.getIdRezervacija());
        }

        Termin termin = prijava.getTermin();
        if (termin != null) {
            termin.setZauzet(false);
            termini.save(termin);
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (prijava.getDatumPredaje() == null) {
            prijava.setDatumPredaje(now);
        }
        prijava.setDatumPreuzimanja(now);
        prijava.setStatus("zavr�eno");
        prijave.save(prijava);
    }

    @Transactional
    public java.io.File markPredaja(Long idPrijava, Long idOsobaPrincipal) throws java.io.IOException {
        PrijavaServisa prijava = prijave.findById(idPrijava)
                .orElseThrow(() -> new IllegalArgumentException("Prijava ne postoji."));

        Serviser serviser = serviseri.findByOsoba_IdOsoba(idOsobaPrincipal)
                .orElseThrow(() -> new AccessDeniedException("Nemate ovlasti za ovu akciju."));
        if (!prijava.getServiser().getIdServiser().equals(serviser.getIdServiser()) && !serviser.isJeLiVoditelj()) {
            throw new AccessDeniedException("Mozete potvrditi predaju samo svojih prijava.");
        }

        if (prijava.getDatumPredaje() == null) {
            prijava.setDatumPredaje(java.time.LocalDateTime.now());
        }
        prijava.setStatus("u obradi");
        prijave.save(prijava);

        java.io.File pdf = pdfService.generateObrazacPdf(prijava, "predaja");
        sendObrazacEmail(prijava, pdf, "Predaja vozila");
        return pdf;
    }

    @Transactional
    public java.io.File markPreuzimanje(Long idPrijava, Long idOsobaPrincipal) throws java.io.IOException {
        PrijavaServisa prijava = prijave.findById(idPrijava)
                .orElseThrow(() -> new IllegalArgumentException("Prijava ne postoji."));

        Serviser serviser = serviseri.findByOsoba_IdOsoba(idOsobaPrincipal)
                .orElseThrow(() -> new AccessDeniedException("Nemate ovlasti za ovu akciju."));
        if (!prijava.getServiser().getIdServiser().equals(serviser.getIdServiser()) && !serviser.isJeLiVoditelj()) {
            throw new AccessDeniedException("Mozete potvrditi preuzimanje samo svojih prijava.");
        }

        if (prijava.getDatumPreuzimanja() == null) {
            prijava.setDatumPreuzimanja(java.time.LocalDateTime.now());
        }
        prijava.setStatus("zavr�eno");
        prijave.save(prijava);

        java.io.File pdf = pdfService.generateObrazacPdf(prijava, "preuzimanje");
        sendObrazacEmail(prijava, pdf, "Preuzimanje vozila");
        return pdf;
    }

    private void sendObrazacEmail(PrijavaServisa prijava, java.io.File pdf, String naslov) {
        try {
            String to = null;
            if (prijava.getVozilo() != null && prijava.getVozilo().getOsoba() != null) {
                to = prijava.getVozilo().getOsoba().getEmail();
            }
            if (to == null || to.isBlank()) return;
            String subj = naslov + " - prijava " + prijava.getIdPrijava();
            String body = "U prilogu se nalazi obrazac: " + naslov.toLowerCase() + ".";
            emailService.sendEmailWithAttachment(to, subj, body, pdf);
        } catch (jakarta.mail.MessagingException ex) {
            // Log and continue; PDF is still returned to the caller.
            org.slf4j.LoggerFactory.getLogger(PrijavaServisaService.class)
                .error("Failed to send obrazac email for prijava id {}", prijava.getIdPrijava(), ex);
        }
    }

    private void sendPrijavaEmail(PrijavaServisa prijava) {
        try {
            java.io.File pdf = pdfService.generatePrijavaPdf(prijava);
            String to = null;
            if (prijava.getVozilo() != null && prijava.getVozilo().getOsoba() != null) {
                to = prijava.getVozilo().getOsoba().getEmail();
            }
            if (to == null || to.isBlank()) return;
            String subj = "Potvrda prijave servisa - " + prijava.getIdPrijava();
            String body = "Vasa prijava servisa je primljena. PDF je u privitku.";
            emailService.sendEmailWithAttachment(to, subj, body, pdf);
            deleteFileQuietly(pdf);
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(PrijavaServisaService.class)
                .error("Failed to send prijava email for prijava id {}", prijava.getIdPrijava(), ex);
        }
    }

    private void deleteFileQuietly(java.io.File file) {
        if (file == null) return;
        try {
            java.nio.file.Files.deleteIfExists(file.toPath());
        } catch (Exception ex) {
            // ignore delete failures
        }
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

    @Transactional
    public void updateVozilo(Long idPrijava, Long idVozilo, Long idOsobaPrincipal) {
        PrijavaServisa prijava = prijave.findById(idPrijava)
                .orElseThrow(() -> new IllegalArgumentException("Prijava ne postoji."));

        // Only administrators should call this method (controller enforces it)
        Vozilo novo = vozila.findById(idVozilo)
                .orElseThrow(() -> new IllegalArgumentException("Vozilo ne postoji."));

        // Provjeri da novo vozilo pripada istom vlasniku
        if (!novo.getOsoba().getIdOsoba().equals(prijava.getVozilo().getOsoba().getIdOsoba())) {
            throw new IllegalArgumentException("Novo vozilo mora pripadati istom vlasniku.");
        }

        prijava.setVozilo(novo);
        prijave.save(prijava);
    }
}



