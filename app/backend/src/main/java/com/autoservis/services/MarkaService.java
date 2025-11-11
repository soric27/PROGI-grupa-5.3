package main.java.com.autoservis.services;

import com.autoservis.domain.marka.Marka;
import com.autoservis.infrastructure.persistence.marka.MarkaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarkaService {
  private final MarkaRepository repo;
  public MarkaService(MarkaRepository repo){ this.repo = repo; }

  public List<Marka> getAll(){ return repo.findAll(); } // sorting možeš dodati po potrebi
}