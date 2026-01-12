package com.autoservis.shared;

import java.util.Comparator;
import java.util.stream.Collectors;  // ← PROMIJENI

import com.autoservis.interfaces.dto.NapomenaDto;
import com.autoservis.interfaces.dto.PrijavaDetalleDto;
import com.autoservis.models.NapomenaServisera;
import com.autoservis.models.PrijavaServisa;

public class PrijavaServisaMapper {

    public static PrijavaDetalleDto toDetailDto(PrijavaServisa p) {  // ← PROMIJENI
        var vozilo = p.getVozilo();
        var vlasnik = vozilo.getOsoba();
        var serviser = p.getServiser().getOsoba();

        String voziloInfo = String.format("%s %s (%s)",
                vozilo.getModel().getMarka().getNaziv(),
                vozilo.getModel().getNaziv(),
                vozilo.getRegistracija());

        String vlasnikInfo = String.format("%s %s, %s",
                vlasnik.getIme(), vlasnik.getPrezime(), vlasnik.getEmail());

        String serviserIme = serviser.getIme() + " " + serviser.getPrezime();

        var napomeneDto = p.getNapomene().stream()
                .sorted(Comparator.comparing(NapomenaServisera::getDatum).reversed())
                .map(n -> new NapomenaDto(n.getIdNapomena(), n.getDatum(), n.getOpis()))
                .collect(Collectors.toList());

        return new PrijavaDetalleDto(  // ← PROMIJENI
                p.getIdPrijava(),
                p.getStatus(),
                p.getTermin().getDatumVrijeme(),
                voziloInfo,
                vlasnikInfo,
                serviserIme,
                p.getNapomenaVlasnika(),
                napomeneDto
        );
    }
}
