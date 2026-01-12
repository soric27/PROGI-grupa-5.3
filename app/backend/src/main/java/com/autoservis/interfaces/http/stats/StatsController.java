package com.autoservis.interfaces.http.stats;

import com.autoservis.services.StatsService;
import com.autoservis.services.StatsExportService;
import com.autoservis.services.dto.StatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

  @Autowired
  private StatsService statsService;

  @Autowired
  private StatsExportService exportService;

  @GetMapping
  @PreAuthorize("hasRole('SERVISER')")
  public ResponseEntity<?> getStats(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                    @RequestParam(defaultValue = "json") String format) throws IOException {

    StatsDto s = statsService.gather(from, to);

    switch (format.toLowerCase()) {
      case "pdf":
        byte[] pdf = exportService.toPdf(s);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stats.pdf").contentType(MediaType.APPLICATION_PDF).body(pdf);
      case "xml":
        byte[] xml = exportService.toXml(s);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xml);
      case "xlsx":
        byte[] xlsx = exportService.toXlsx(s);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stats.xlsx").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(xlsx);
      default:
        return ResponseEntity.ok(s);
    }
  }
}
