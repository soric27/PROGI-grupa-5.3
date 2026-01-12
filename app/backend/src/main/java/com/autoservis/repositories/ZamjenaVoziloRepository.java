package com.autoservis.repositories;

import com.autoservis.models.ZamjenaVozilo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZamjenaVoziloRepository extends JpaRepository<ZamjenaVozilo, Long> {
  List<ZamjenaVozilo> findByDostupnoTrue();
}
