package com.autoservis.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autoservis.models.Marka;
import com.autoservis.repositories.MarkaRepository;

@Service
public class MarkaService {
  private final MarkaRepository repo;
  public MarkaService(MarkaRepository repo) { this.repo = repo; }

  public List<Marka> getAll(){ return repo.findAll(); } // Sorting možeš dodati po potrebi
}
