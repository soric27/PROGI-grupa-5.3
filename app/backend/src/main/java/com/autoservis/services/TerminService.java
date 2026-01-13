package com.autoservis.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autoservis.interfaces.dto.TerminDto;
import com.autoservis.repositories.TerminRepository;
import com.autoservis.shared.TerminMapper;

@Service
public class TerminService {
    private final TerminRepository termini;

    public TerminService(TerminRepository termini) {
        this.termini = termini;
    }

    @Transactional(readOnly = true)
    public List<TerminDto> getSlobodniTermini(Long serviserId) {
        List<com.autoservis.models.Termin> list = (serviserId == null)
                ? termini.findAvailable()
                : termini.findAvailableByServiser(serviserId);
        return list.stream()
                .map(TerminMapper::toDto)
                .toList();
    }
}