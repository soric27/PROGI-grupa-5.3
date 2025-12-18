package com.autoservis.interfaces.http.vozilo;

public record VehicleDto(
  Long id_vozilo,
  Long id_model,
  String registracija,
  Integer godina_proizvodnje,
  String model_naziv,
  String marka_naziv
) {}