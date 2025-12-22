package com.autoservis.repositories;

import com.autoservis.models.Serviser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiserRepository extends JpaRepository<Serviser, Long> {}