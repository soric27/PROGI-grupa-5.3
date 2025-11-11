package com.autoservis.services;

import com.autoservis.models.Model;
import com.autoservis.repositories.ModelRepository;
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