package com.autoservis.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "termin")
public class Termin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_termin")
    private Long idTermin;

    @Column(name = "datum_vrijeme", nullable = false)
    private LocalDateTime datumVrijeme;

    @Column(name = "zauzet")
    private boolean zauzet = false;

    protected Termin() {}

    public Termin(LocalDateTime datumVrijeme) {
        this.datumVrijeme = datumVrijeme;
    }

    // Getteri i Setteri
    public Long getIdTermin() { return idTermin; }
    public LocalDateTime getDatumVrijeme() { return datumVrijeme; }
    public boolean isZauzet() { return zauzet; }
    public void setZauzet(boolean zauzet) { this.zauzet = zauzet; }
}