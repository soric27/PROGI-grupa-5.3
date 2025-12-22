package com.autoservis.shared;

import com.autoservis.interfaces.dto.TerminDto;
import com.autoservis.models.Termin;

public class TerminMapper {
    public static TerminDto toDto(Termin termin) {
        return new TerminDto(termin.getIdTermin(), termin.getDatumVrijeme());
    }
}