package com.autoservis.repositories;

import com.autoservis.models.RezervacijaZamjene;
import com.autoservis.models.ZamjenaVozilo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RezervacijaZamjeneRepository extends JpaRepository<RezervacijaZamjene, Long> {

  @Query("SELECT r FROM RezervacijaZamjene r WHERE r.zamjena = :zamjena AND NOT (r.datumDo < :from OR r.datumOd > :to)")
  List<RezervacijaZamjene> findOverlapping(@Param("zamjena") ZamjenaVozilo zamjena, @Param("from") LocalDate from, @Param("to") LocalDate to);

  List<RezervacijaZamjene> findByPrijava_IdPrijava(Long idPrijava);
}
