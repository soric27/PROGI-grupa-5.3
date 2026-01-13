package com.autoservis.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_serviser")
    private Serviser serviser;

    protected Termin() {}

    public Termin(LocalDateTime datumVrijeme, Serviser serviser) {
        this.datumVrijeme = datumVrijeme;
        this.serviser = serviser;
    }

    // Getteri i Setteri
    public Long getIdTermin() { return idTermin; }
    public LocalDateTime getDatumVrijeme() { return datumVrijeme; }
    public boolean isZauzet() { return zauzet; }
    public void setZauzet(boolean zauzet) { this.zauzet = zauzet; }
    public Serviser getServiser() { return serviser; }
    public void setServiser(Serviser serviser) { this.serviser = serviser; }
}