package com.autoservis.interfaces.dto;

import jakarta.validation.constraints.NotNull;

// DTO za podatke koje frontend šalje prilikom kreiranja prijave
public record PrijavaServisaCreateDto(
    @NotNull Long idVozilo,
    @NotNull Long idServiser,
    @NotNull Long idTermin,
    String napomenaVlasnika
) {}
