package com.autoservis.interfaces.http.kvar;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoservis.interfaces.dto.KvarDto;
import com.autoservis.models.Kvar;
import com.autoservis.repositories.KvarRepository;

@RestController
@RequestMapping("/api/kvarovi")
public class KvarController {

  @Autowired
  private KvarRepository kvarRepository;

  @GetMapping
  public ResponseEntity<List<KvarDto>> listAll() {
    List<Kvar> kvarovi = kvarRepository.findAll();
    List<KvarDto> dtos = kvarovi.stream()
        .map(k -> new KvarDto(k.getIdKvar(), k.getNaziv(), k.getOpis()))
        .toList();
    return ResponseEntity.ok(dtos);
  }
}
