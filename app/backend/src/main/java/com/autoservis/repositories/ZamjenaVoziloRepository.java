package com.autoservis.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoservis.models.ZamjenaVozilo;

@Repository
public interface ZamjenaVoziloRepository extends JpaRepository<ZamjenaVozilo, Long> {
  List<ZamjenaVozilo> findByDostupnoTrue();
  boolean existsByRegistracijaIgnoreCase(String registracija);
}
