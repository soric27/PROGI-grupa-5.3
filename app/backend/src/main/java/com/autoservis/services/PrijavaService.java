package com.autoservis.services;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoservis.models.PrijavaServisa;
import com.autoservis.models.Termin;
import com.autoservis.repositories.PrijavaServisaRepository;
import com.autoservis.repositories.TerminRepository;

import jakarta.mail.MessagingException;

@Service
public class PrijavaService {

  private static final Logger logger = LoggerFactory.getLogger(PrijavaService.class);

  private final PrijavaServisaRepository prijavaRepo;
  private final TerminRepository terminRepo;
  private final PdfService pdfService; // ostavljeno jer je u konstruktoru; ne koristi se
  private final EmailService emailService;

  public PrijavaService(
      PrijavaServisaRepository prijavaRepo,
      TerminRepository terminRepo,
      PdfService pdfService,
      EmailService emailService
  ) {
    this.prijavaRepo = prijavaRepo;
    this.terminRepo = terminRepo;
    this.pdfService = pdfService;
    this.emailService = emailService;
  }

  @Transactional
  public PrijavaServisa createPrijava(PrijavaServisa prijava) throws IOException {

    logger.info("createPrijava CALLED");
    // if termin object provided but not persisted, persist it first
    if (prijava.getTermin() != null && prijava.getTermin().getIdTermin() == null) {
      Termin t = prijava.getTermin();
      t = terminRepo.save(t);
      prijava.setTermin(t);
    }

    // save prijava so it has id
    PrijavaServisa saved = prijavaRepo.save(prijava);

    logger.info("createPrijava SAVED id={}", saved.getIdPrijava());

    // send simple confirmation email (no attachment)
    try {
      if (saved.getVozilo() != null
          && saved.getVozilo().getOsoba() != null
          && saved.getVozilo().getOsoba().getEmail() != null) {

        String to = saved.getVozilo().getOsoba().getEmail();
        String subj = "Potvrda prijave servisa - " + saved.getIdPrijava();

        String body =
            "Poštovani,\n\n" +
            "zaprimili smo vašu prijavu servisa.\n" +
            "Broj prijave: " + saved.getIdPrijava() + "\n" +
            (saved.getTermin() != null && saved.getTermin().getDatumVrijeme() != null
                ? "Termin: " + saved.getTermin().getDatumVrijeme().toString() + "\n"
                : "") +
            "\nLijep pozdrav,\nAuto servis";

        emailService.sendSimple(to, subj, body);
        logger.info("Sent confirmation email to {} for prijava id {}", to, saved.getIdPrijava());
      }
    } catch (MessagingException ex) {
      // log and continue
      logger.error("Failed to send confirmation email for prijava id {}", saved.getIdPrijava(), ex);
    }

    return saved;
  }

  @Transactional
  public Optional<PrijavaServisa> updatePrijava(
      Long id,
      LocalDateTime newTerminDate,
      String newStatus,
      Long requesterId,
      boolean isAdmin,
      boolean isServiser
  ) throws IOException, MessagingException {

    Optional<PrijavaServisa> opt = prijavaRepo.findById(id);
    if (opt.isEmpty()) return opt;

    PrijavaServisa existing = opt.get();
    LocalDateTime oldTermin = existing.getTermin() != null ? existing.getTermin().getDatumVrijeme() : null;

    // Authorization: only administrator or the assigned serviser may change status or postpone term
    boolean isAssignedServiser =
        existing.getServiser() != null
        && existing.getServiser().getOsoba() != null
        && existing.getServiser().getOsoba().getIdOsoba().equals(requesterId);

    if (!isAdmin && !isAssignedServiser) {
      throw new org.springframework.security.access.AccessDeniedException("Nemate ovlasti za ažuriranje ove prijave.");
    }

    if (newTerminDate != null) {
      Termin oldTerminObj = existing.getTermin();

      Termin termin;
      // Prefer existing predefined slot for the assigned serviser if present
      if (existing.getServiser() != null) {
        java.util.Optional<Termin> existingSlot =
            terminRepo.findByDatumVrijemeAndServiser_IdServiser(newTerminDate, existing.getServiser().getIdServiser());

        if (existingSlot.isPresent()) {
          termin = existingSlot.get();
          // Mark the existing slot as taken
          if (!termin.isZauzet()) {
            termin.setZauzet(true);
            terminRepo.save(termin);
          }
        } else {
          // create a new termin tied to the serviser so it shows up in the serviser's schedule
          termin = new Termin(newTerminDate, existing.getServiser());
          termin.setZauzet(true);
          termin = terminRepo.save(termin);
        }
      } else {
        // no assigned serviser: keep behavior of creating standalone termin
        termin = new Termin(newTerminDate);
        termin.setZauzet(true);
        termin = terminRepo.save(termin);
      }

      // Free up the old termin slot if it exists
      if (oldTerminObj != null && !oldTerminObj.equals(termin)) {
        oldTerminObj.setZauzet(false);
        terminRepo.save(oldTerminObj);
      }

      existing.setTermin(termin);
    }

    boolean statusChanged = false;
    String oldStatus = existing.getStatus();
    if (newStatus != null && !newStatus.equals(oldStatus)) {
      existing.setStatus(newStatus);
      statusChanged = true;
    }

    prijavaRepo.save(existing);

    LocalDateTime newTermin = existing.getTermin() != null ? existing.getTermin().getDatumVrijeme() : null;

    // decide if we should notify
    boolean shouldNotify = false;

    if (oldTermin != null && newTermin != null && !oldTermin.equals(newTermin)) {
      Duration diff = Duration.between(oldTermin, newTermin);
      if (diff.toDays() >= 3) shouldNotify = true;
    }

    if (!shouldNotify && statusChanged && "odgođeno".equalsIgnoreCase(existing.getStatus())) {
      shouldNotify = true;
    }

    if (shouldNotify) {
      try {
        String to = null;
        if (existing.getVozilo() != null && existing.getVozilo().getOsoba() != null) {
          to = existing.getVozilo().getOsoba().getEmail();
        }

        if (to != null) {
          String subj = "Obavijest: promjena termina/statusa servisa - " + existing.getIdPrijava();

          String body =
              "Poštovani,\n\n" +
              "došlo je do promjene vezane uz vašu prijavu servisa.\n" +
              "Broj prijave: " + existing.getIdPrijava() + "\n" +
              (oldStatus != null ? "Stari status: " + oldStatus + "\n" : "") +
              (existing.getStatus() != null ? "Novi status: " + existing.getStatus() + "\n" : "") +
              (oldTermin != null ? "Stari termin: " + oldTermin.toString() + "\n" : "") +
              (newTermin != null ? "Novi termin: " + newTermin.toString() + "\n" : "") +
              "\nLijep pozdrav,\nAuto servis";

          try {
            emailService.sendSimple(to, subj, body);
            logger.info("Sent update notification email to {} for prijava id {}", to, existing.getIdPrijava());
          } catch (MessagingException ex) {
            logger.error("Failed to send update notification email for prijava id {} to {}", existing.getIdPrijava(), to, ex);
          }
        }
      } catch (Exception ex) {
        logger.error("Failed to send update notification for prijava id {}: {}", existing.getIdPrijava(), ex.getMessage(), ex);
      }
    }

    return Optional.of(existing);
  }
}
