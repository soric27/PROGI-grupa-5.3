package com.autoservis.interfaces.http.zamjena;

public record ZamjenaDto(
  Long id_zamjena,
  Long id_model,
  String registracija,
  Boolean dostupno,
  String model_naziv,
  String marka_naziv
) {}
