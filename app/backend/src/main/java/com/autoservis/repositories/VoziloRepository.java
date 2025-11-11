package com.autoservis.repositories;
import com.autoservis.models.Vozilo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VoziloRepository extends JpaRepository<Vozilo, Long> {
  List<Vozilo> findByOsoba_IdOsobaOrderByIdVoziloDesc(Long idOsoba);
}