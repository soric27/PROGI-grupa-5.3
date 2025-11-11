package com.autoservis.interfaces.http.vozilo;

import jakarta.validation.constraints.*;

public record VehicleCreateDto(
  @NotNull Long id_model,
  @NotBlank String registracija,
  @NotNull Integer godina_proizvodnje
) {}