package com.autoservis.repositories;

import com.autoservis.models.Serviser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiserRepository extends JpaRepository<Serviser, Long> {
     Optional<Serviser> findByOsoba_IdOsoba(Long idOsoba);
}