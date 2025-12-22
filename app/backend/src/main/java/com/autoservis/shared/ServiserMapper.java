package com.autoservis.shared;

import com.autoservis.interfaces.dto.ServiserDto;
import com.autoservis.models.Serviser;

public class ServiserMapper {
    public static ServiserDto toDto(Serviser serviser) {
        var osoba = serviser.getOsoba();
        String imePrezime = (osoba != null) ? osoba.getIme() + " " + osoba.getPrezime() : "Nepoznat";
        return new ServiserDto(serviser.getIdServiser(), imePrezime);
    }
}