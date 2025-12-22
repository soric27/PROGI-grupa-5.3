package com.autoservis.interfaces.dto;

import java.time.LocalDateTime;

public record NapomenaDto(
    Long idNapomena,
    LocalDateTime datum,
    String opis
) {}