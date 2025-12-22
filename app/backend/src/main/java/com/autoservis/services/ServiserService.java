package com.autoservis.services;

import com.autoservis.interfaces.dto.ServiserDto;
import com.autoservis.repositories.ServiserRepository;
import com.autoservis.shared.ServiserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiserService {
    private final ServiserRepository serviseri;

    public ServiserService(ServiserRepository serviseri) {
        this.serviseri = serviseri;
    }

    @Transactional(readOnly = true)
    public List<ServiserDto> getSviServiseri() {
        return serviseri.findAll().stream()
                .map(ServiserMapper::toDto)
                .toList();
    }
}