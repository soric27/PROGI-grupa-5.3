package main.java.com.autoservis.repositories;
import main.java.com.autoservis.models.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ModelRepository extends JpaRepository<Model, Long> {
  List<Model> findByMarka_IdMarkaOrderByNazivAsc(Long idMarka);
}