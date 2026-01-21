package com.autoservis.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OsobaUpsertDto(
  @NotBlank String ime,
  @NotBlank String prezime,
  @Email(message = "Nevažeći email") String email,
  @NotBlank String uloga
) {}
