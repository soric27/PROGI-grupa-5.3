package com.autoservis.services;

import java.io.File;
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
  private final PdfService pdfService;
  private final EmailService emailService;

  public PrijavaService(PrijavaServisaRepository prijavaRepo, TerminRepository terminRepo, PdfService pdfService, EmailService emailService) {
    this.prijavaRepo = prijavaRepo;
    this.terminRepo = terminRepo;
    this.pdfService = pdfService;
    this.emailService = emailService;
  }

  @Transactional
  public PrijavaServisa createPrijava(PrijavaServisa prijava) throws IOException {
    // if termin object provided but not persisted, persist it first
    if (prijava.getTermin() != null && prijava.getTermin().getIdTermin() == null) {
      Termin t = prijava.getTermin();
      t = terminRepo.save(t);
      prijava.setTermin(t);
    }

    // save prijava so it has id
    PrijavaServisa saved = prijavaRepo.save(prijava);
    // generate pdf and save obrazac
    File pdf = pdfService.generatePrijavaPdf(saved);

    // optionally send email to owner with pdf attachment
    try {
      if (saved.getVozilo() != null && saved.getVozilo().getOsoba() != null && saved.getVozilo().getOsoba().getEmail() != null) {
        String to = saved.getVozilo().getOsoba().getEmail();
        String subj = "Potvrda prijave servisa - " + saved.getIdPrijava();
        String body = "Vaša prijava servisa je primljena. Detalji su u privitku.";
        emailService.sendEmailWithAttachment(to, subj, body, pdf);
      }
    } catch (MessagingException ex) {
      // log and continue
      logger.error("Failed to send confirmation email for prijava id {}", saved.getIdPrijava(), ex);
    }

    return saved;
  }

  @Transactional
  public Optional<PrijavaServisa> updatePrijava(Long id, LocalDateTime newTerminDate, String newStatus, Long requesterId, boolean isAdmin, boolean isServiser) throws IOException, MessagingException {
    Optional<PrijavaServisa> opt = prijavaRepo.findById(id);
    if (opt.isEmpty()) return opt;

    PrijavaServisa existing = opt.get();
    LocalDateTime oldTermin = existing.getTermin() != null ? existing.getTermin().getDatumVrijeme() : null;

    // Authorization: only administrator or the assigned serviser may change status or postpone term
    boolean isAssignedServiser = existing.getServiser() != null && existing.getServiser().getOsoba() != null && existing.getServiser().getOsoba().getIdOsoba().equals(requesterId);
    if (!isAdmin && !isAssignedServiser) {
      throw new org.springframework.security.access.AccessDeniedException("Nemate ovlasti za ažuriranje ove prijave.");
    }

    if (newTerminDate != null) {
      Termin termin = new Termin(newTerminDate);
      termin = terminRepo.save(termin);
      existing.setTermin(termin);
    }

    boolean statusChanged = false;
    if (newStatus != null && !newStatus.equals(existing.getStatus())) {
      existing.setStatus(newStatus);
      statusChanged = true;
    }

    prijavaRepo.save(existing);

    LocalDateTime newTermin = existing.getTermin() != null ? existing.getTermin().getDatumVrijeme() : null;

    boolean shouldNotify = false;
    if (oldTermin != null && newTermin != null) {
      Duration diff = Duration.between(oldTermin, newTermin);
      if (diff.toDays() >= 3) shouldNotify = true;
    }

    if (!shouldNotify && statusChanged && "odgođeno".equalsIgnoreCase(existing.getStatus())) {
      // if status changed to 'odgođeno' but no previous termin to compare, we still notify
      shouldNotify = true;
    }

    if (shouldNotify) {
      try {
        // generate updated pdf and attempt to send - failures here should not break the update
        File pdf = pdfService.generatePrijavaPdf(existing);

        String to = null;
        if (existing.getVozilo() != null && existing.getVozilo().getOsoba() != null) to = existing.getVozilo().getOsoba().getEmail();
        if (to != null) {
          String subj = "Obavijest: termin servisa odgođen";
          String body = String.format("Vaš termin servisa je promijenjen.%s\nStari termin: %s\nNovi termin: %s\nDetalji u privitku.",
              "",
              oldTermin != null ? oldTermin.toString() : "-",
              newTermin != null ? newTermin.toString() : "-");
          try {
            emailService.sendEmailWithAttachment(to, subj, body, pdf);
            logger.info("Sent postponement email to {} for prijava id {}", to, existing.getIdPrijava());
          } catch (MessagingException ex) {
            logger.error("Failed to send postponement email for prijava id {} to {}", existing.getIdPrijava(), to, ex);
          }
        }
      } catch (Exception ex) {
        logger.error("Failed to generate/send postponement notification for prijava id {}: {}", existing.getIdPrijava(), ex.getMessage(), ex);
      }
    }

    return Optional.of(existing);
  }
}