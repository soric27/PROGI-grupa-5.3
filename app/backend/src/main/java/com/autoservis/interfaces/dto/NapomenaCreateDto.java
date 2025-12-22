package com.autoservis.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record NapomenaCreateDto(
    @NotBlank String opis
) {}