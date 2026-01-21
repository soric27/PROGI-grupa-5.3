package com.autoservis.interfaces.dto;

import jakarta.validation.constraints.Email;

public record OsobaUpdateDto(
    String ime,
    String prezime,
    @Email(message = "Nevažeći email") String email
) {}
