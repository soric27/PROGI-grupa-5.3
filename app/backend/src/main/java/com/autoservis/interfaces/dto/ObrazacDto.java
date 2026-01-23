package com.autoservis.interfaces.dto;

import java.time.LocalDateTime;

public record ObrazacDto(
    Long idObrazac,
    Long idPrijava,
    String tip,
    LocalDateTime datumGeneriranja,
    String voziloInfo,
    String vlasnikInfo
) {}
