package com.autoservis.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.autoservis.models.Termin;

public interface TerminRepository extends JpaRepository<Termin, Long> {

    // Upit za dohvaćanje svih slobodnih termina u budućnosti
    @Query("SELECT t FROM Termin t WHERE t.zauzet = false AND t.datumVrijeme > CURRENT_TIMESTAMP ORDER BY t.datumVrijeme ASC")
    List<Termin> findAvailable();

    @Query("SELECT t FROM Termin t WHERE t.zauzet = false AND t.serviser.idServiser = :serviserId AND t.datumVrijeme > CURRENT_TIMESTAMP ORDER BY t.datumVrijeme ASC")
    List<Termin> findAvailableByServiser(Long serviserId);

    // Atomic mark-as-taken update (returns number of rows updated)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Termin t SET t.zauzet = true WHERE t.idTermin = :id AND t.zauzet = false")
    int markAsTaken(Long id);
}