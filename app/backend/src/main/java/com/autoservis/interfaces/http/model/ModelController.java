package main.java.com.autoservis.interfaces.http.model;

import com.autoservis.application.model.ModelService;
import com.autoservis.domain.model.Model;
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