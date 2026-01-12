package com.autoservis.services;

import com.autoservis.models.PrijavaServisa;
import com.autoservis.models.Obrazac;
import com.autoservis.repositories.ObrazacRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

  private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

  @Value("${app.storage.pdf-dir:./data/pdfs}")
  private String pdfDir;

  private final ObrazacRepository obrazacRepository;

  public PdfService(ObrazacRepository obrazacRepository) {
    this.obrazacRepository = obrazacRepository;
  }

  public File generatePrijavaPdf(PrijavaServisa prijava) throws IOException {
    Files.createDirectories(Path.of(pdfDir));
    String filename = String.format("prijava_%d_%d.pdf", prijava.getIdPrijava() == null ? System.currentTimeMillis() : prijava.getIdPrijava(), System.currentTimeMillis());
    Path out = Path.of(pdfDir, filename);

    try (PDDocument doc = new PDDocument()) {
      PDPage page = new PDPage();
      doc.addPage(page);

      try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(50, 700);
        cs.showText("Prijava servisa");
        cs.endText();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 12);
        cs.newLineAtOffset(50, 660);
        cs.showText("ID prijave: " + (prijava.getIdPrijava() != null ? prijava.getIdPrijava() : "-"));
        cs.newLineAtOffset(0, -18);
        cs.showText("Registracija: " + (prijava.getVozilo() != null ? prijava.getVozilo().getRegistracija() : "-"));
        cs.newLineAtOffset(0, -18);
        cs.showText("Vlasnik: " + (prijava.getVozilo() != null && prijava.getVozilo().getOsoba() != null ? prijava.getVozilo().getOsoba().getIme() + " " + prijava.getVozilo().getOsoba().getPrezime() : "-"));
        cs.newLineAtOffset(0, -18);
        cs.showText("Status: " + (prijava.getStatus() != null ? prijava.getStatus() : "-"));
        cs.newLineAtOffset(0, -18);
        cs.showText("Datum prijave: " + (prijava.getDatumPrijave() != null ? prijava.getDatumPrijave().format(fmt) : "-"));
        if (prijava.getTermin() != null && prijava.getTermin().getDatumVrijeme() != null) {
          cs.newLineAtOffset(0, -18);
          cs.showText("Termin: " + prijava.getTermin().getDatumVrijeme().format(fmt));
        }
        cs.newLineAtOffset(0, -18);
        cs.showText("Napomena: " + (prijava.getNapomenaVlasnika() != null ? prijava.getNapomenaVlasnika() : "-"));
        cs.endText();
      }

      doc.save(out.toFile());
      logger.info("Generated PDF for prijava {} at {}", prijava.getIdPrijava(), out.toAbsolutePath());
    }

    // create Obrazac record with tip 'predaja'
    Obrazac obrazac = new Obrazac(prijava, "predaja", out.toAbsolutePath().toString());
    obrazacRepository.save(obrazac);
    logger.debug("Saved Obrazac id {} for prijava id {}", obrazac.getIdObrazac(), prijava.getIdPrijava());

    return out.toFile();
  }
}