package com.autoservis.interfaces.dto;

import java.time.LocalDateTime;

// DTO za prikaz detalja o postojećoj prijavi
public record PrijavaServisaDto(
    Long idPrijava,
    String status,
    LocalDateTime datumTermina,
    String voziloInfo, // Npr. "Audi A4 (ZG-1234-AB)"
    String serviserIme
) {}
