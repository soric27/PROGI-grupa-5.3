package com.autoservis.interfaces.dto;

import java.time.LocalDate;

public record RezervacijaZamjeneDto(
    Long idRezervacija,
    Long idZamjena,
    String registracija,
    LocalDate datumOd,
    LocalDate datumDo
) {}
