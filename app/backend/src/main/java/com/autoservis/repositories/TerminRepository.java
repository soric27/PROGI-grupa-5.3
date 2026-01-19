package com.autoservis.repositories;

import com.autoservis.models.Termin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TerminRepository extends JpaRepository<Termin, Long> {

    // Upit za dohvaćanje svih slobodnih termina u budućnosti
    @Query("SELECT t FROM Termin t WHERE t.zauzet = false AND t.datumVrijeme > CURRENT_TIMESTAMP ORDER BY t.datumVrijeme ASC")
    List<Termin> findAvailable();
}
