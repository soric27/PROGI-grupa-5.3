package com.autoservis.repositories;

import com.autoservis.models.Obrazac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObrazacRepository extends JpaRepository<Obrazac, Long> {
  List<Obrazac> findByTipOrderByDatumGeneriranjaDesc(String tip);
  List<Obrazac> findByPrijava_Serviser_IdServiserAndTipOrderByDatumGeneriranjaDesc(Long idServiser, String tip);
}
