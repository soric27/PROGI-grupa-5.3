package com.autoservis.interfaces.http.model;

import com.autoservis.services.ModelService;
import com.autoservis.models.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/modeli")
public class ModelController {
  private final ModelService service;
  public ModelController(ModelService s){ this.service = s; }

  @GetMapping("/{id_marka}")
  public List<Model> byMarka(@PathVariable("id_marka") Long idMarka){
    return service.getByMarka(idMarka);
  }
}