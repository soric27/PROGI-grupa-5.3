package com.autoservis.interfaces.dto;

import java.time.LocalDateTime;

public record TerminDto(
    Long idTermin,
    LocalDateTime datumVrijeme
) {}