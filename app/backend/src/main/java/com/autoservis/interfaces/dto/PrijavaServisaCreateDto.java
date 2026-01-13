package com.autoservis.interfaces.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

// DTO za podatke koje frontend šalje prilikom kreiranja prijave
public record PrijavaServisaCreateDto(
    @NotNull Long idVozilo,
    @NotNull Long idServiser,
    @NotNull Long idTermin,
    String napomenaVlasnika,
    Long idZamjena,
    LocalDate datumOd,
    LocalDate datumDo
) {}