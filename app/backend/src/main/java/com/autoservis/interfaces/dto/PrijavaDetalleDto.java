package com.autoservis.interfaces.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PrijavaDetalleDto(
    Long idPrijava,
    String status,
    LocalDateTime datumTermina,
    Long idVozilo,
    Long idVlasnik,
    String voziloInfo, // "Audi A4 (ZG-1234-AB)"
    String vlasnikInfo, // "Pero Perić, pero@email.com"
    String serviserIme,
    String napomenaVlasnika,
    List<NapomenaDto> napomeneServisera,
    RezervacijaZamjeneDto rezervacijaZamjene
) {}