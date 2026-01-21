package com.autoservis.repositories;

import com.autoservis.models.PrijavaServisa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrijavaServisaRepository extends JpaRepository<PrijavaServisa, Long> {
    List<PrijavaServisa> findByVozilo_IdVozilo(Long idVozilo);
}
