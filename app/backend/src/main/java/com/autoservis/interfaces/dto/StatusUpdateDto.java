package com.autoservis.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusUpdateDto(
    @NotBlank String noviStatus
) {}