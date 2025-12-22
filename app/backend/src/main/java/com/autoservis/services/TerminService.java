package com.autoservis.services;

import com.autoservis.interfaces.dto.TerminDto;
import com.autoservis.repositories.TerminRepository;
import com.autoservis.shared.TerminMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TerminService {
    private final TerminRepository termini;

    public TerminService(TerminRepository termini) {
        this.termini = termini;
    }

    @Transactional(readOnly = true)
    public List<TerminDto> getSlobodniTermini() {
        return termini.findAvailable().stream()
                .map(TerminMapper::toDto)
                .toList();
    }
}