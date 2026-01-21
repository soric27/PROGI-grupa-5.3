package com.autoservis.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoservis.models.Kvar;

@Repository
public interface KvarRepository extends JpaRepository<Kvar, Long> {
  List<Kvar> findAll();
}
