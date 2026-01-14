package com.autoservis.shared;

import java.util.Comparator;
import java.util.stream.Collectors;

import com.autoservis.interfaces.dto.KvarDto;
import com.autoservis.interfaces.dto.NapomenaDto;
import com.autoservis.interfaces.dto.PrijavaDetalleDto;
import com.autoservis.interfaces.dto.RezervacijaZamjeneDto;
import com.autoservis.models.NapomenaServisera;
import com.autoservis.models.PrijavaServisa;
import com.autoservis.models.RezervacijaZamjene;

public class PrijavaServisaMapper {

    public static PrijavaDetalleDto toDetailDto(PrijavaServisa p) {
        return toDetailDto(p, null);
    }

    public static PrijavaDetalleDto toDetailDto(PrijavaServisa p, RezervacijaZamjene rez) {
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

        var kvaroveDto = p.getKvarovi().stream()
                .map(k -> new KvarDto(k.getIdKvar(), k.getNaziv(), k.getOpis()))
                .collect(Collectors.toList());

        RezervacijaZamjeneDto rezDto = null;
        if (rez != null) {
            rezDto = new RezervacijaZamjeneDto(
                rez.getIdRezervacija(),
                rez.getZamjena().getIdZamjena(),
                rez.getZamjena().getRegistracija(),
                rez.getDatumOd(),
                rez.getDatumDo()
            );
        }

        return new PrijavaDetalleDto(
                p.getIdPrijava(),
                p.getStatus(),
                p.getTermin().getDatumVrijeme(),
                vozilo.getIdVozilo(),
                vlasnik.getIdOsoba(),
                voziloInfo,
                vlasnikInfo,
                p.getServiser().getIdServiser(),
                serviserIme,
                p.getNapomenaVlasnika(),
                napomeneDto,
                rezDto,
                kvaroveDto
        );
    }
}
