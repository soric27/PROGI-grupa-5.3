package main.java.com.autoservis.services;

import com.autoservis.domain.model.Model;
import com.autoservis.infrastructure.persistence.model.ModelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelService {
  private final ModelRepository repo;
  public ModelService(ModelRepository repo){ this.repo = repo; }

  public List<Model> getByMarka(Long idMarka){
    return repo.findByMarka_IdMarkaOrderByNazivAsc(idMarka);
  }
}