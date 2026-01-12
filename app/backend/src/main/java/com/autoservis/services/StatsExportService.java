package com.autoservis.services;

import com.autoservis.services.dto.StatsDto;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class StatsExportService {

  private static final Logger logger = LoggerFactory.getLogger(StatsExportService.class);

  public byte[] toPdf(StatsDto s) throws IOException {
    try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      PDPage page = new PDPage();
      doc.addPage(page);
      try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(50, 750);
        cs.showText("Statistika - " + s.from + " - " + s.to);
        cs.endText();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 12);
        cs.newLineAtOffset(50, 720);
        cs.showText("Broj zaprimljenih vozila: " + s.prijaveCount);
        cs.newLineAtOffset(0, -16);
        cs.showText("Završeni popravci (count): " + s.completedRepairsCount);
        cs.newLineAtOffset(0, -16);
        cs.showText("Prosječno trajanje popravka (dani): " + String.format("%.2f", s.averageRepairDays));
        cs.newLineAtOffset(0, -16);
        cs.showText("Zauzeće zamjenskih vozila (%): " + String.format("%.2f", s.replacementOccupancyPercent));
        cs.newLineAtOffset(0, -16);
        cs.showText("Dostupni termini (count): " + s.availableSlotsCount);
        cs.endText();
      }

      doc.save(out);
      logger.info("Exported stats to PDF ({} bytes)", out.size());
      return out.toByteArray();
    }
  }

  public byte[] toXml(StatsDto s) {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sb.append(String.format("<stats from=\"%s\" to=\"%s\">\n", s.from, s.to));
    sb.append(String.format("  <prijave>%d</prijave>\n", s.prijaveCount));
    sb.append(String.format("  <completedRepairs>%d</completedRepairs>\n", s.completedRepairsCount));
    sb.append(String.format("  <averageRepairDays>%.2f</averageRepairDays>\n", s.averageRepairDays));
    sb.append(String.format("  <replacementOccupancyPercent>%.2f</replacementOccupancyPercent>\n", s.replacementOccupancyPercent));
    sb.append(String.format("  <availableSlotsCount>%d</availableSlotsCount>\n", s.availableSlotsCount));
    sb.append("  <availableSlots>\n");
    for (String slot : s.availableSlots) sb.append("    <slot>" + slot + "</slot>\n");
    sb.append("  </availableSlots>\n");
    sb.append("</stats>");

    logger.info("Exported stats to XML ({} bytes)", sb.toString().getBytes(StandardCharsets.UTF_8).length);
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  public byte[] toXlsx(StatsDto s) throws IOException {
    try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Sheet sh = wb.createSheet("Statistika");
      int r = 0;
      Row row = sh.createRow(r++); row.createCell(0).setCellValue("Statistika");
      row = sh.createRow(r++); row.createCell(0).setCellValue("Period"); row.createCell(1).setCellValue(s.from + " - " + s.to);
      row = sh.createRow(r++); row.createCell(0).setCellValue("Broj zaprimljenih vozila"); row.createCell(1).setCellValue(s.prijaveCount);
      row = sh.createRow(r++); row.createCell(0).setCellValue("Završeni popravci (count)"); row.createCell(1).setCellValue(s.completedRepairsCount);
      row = sh.createRow(r++); row.createCell(0).setCellValue("Prosječno trajanje popravka (dani)"); row.createCell(1).setCellValue(s.averageRepairDays);
      row = sh.createRow(r++); row.createCell(0).setCellValue("Zauzeće zamjenskih vozila (%)"); row.createCell(1).setCellValue(s.replacementOccupancyPercent);
      row = sh.createRow(r++); row.createCell(0).setCellValue("Dostupni termini (count)"); row.createCell(1).setCellValue(s.availableSlotsCount);

      row = sh.createRow(r++); row.createCell(0).setCellValue("Dostupni termini (lista)");
      for (String slot : s.availableSlots) {
        row = sh.createRow(r++); row.createCell(0).setCellValue(slot);
      }

      wb.write(out);
      logger.info("Exported stats to XLSX ({} bytes)", out.size());
      return out.toByteArray();
    }
  }
}