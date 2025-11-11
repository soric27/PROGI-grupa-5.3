package main.java.com.autoservis.interfaces.http.marka;

import com.autoservis.application.marka.MarkaService;
import com.autoservis.domain.marka.Marka;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/marke")
public class MarkaController {
  private final MarkaService service;
  public MarkaController(MarkaService s){ this.service = s; }

  @GetMapping
  public List<Marka> all(){ return service.getAll(); }
}