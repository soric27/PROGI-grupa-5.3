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
    private Boolean zauzet = false; // use wrapper so callers can check null if needed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_serviser")
    private Serviser serviser;

    protected Termin() {}

    // Convenience constructor, allow creating a Termin with only datetime
    public Termin(LocalDateTime datumVrijeme) {
        this.datumVrijeme = datumVrijeme;
        this.serviser = null;
        this.zauzet = false;
    }

    public Termin(LocalDateTime datumVrijeme, Serviser serviser) {
        this.datumVrijeme = datumVrijeme;
        this.serviser = serviser;
        this.zauzet = false;
    }

    // Getteri i Setteri
    public Long getIdTermin() { return idTermin; }
    public LocalDateTime getDatumVrijeme() { return datumVrijeme; }
    // compatibility: wrapper getter used by StatsService
    public Boolean getZauzet() { return zauzet; }
    public boolean isZauzet() { return zauzet != null && zauzet; }
    public void setZauzet(Boolean zauzet) { this.zauzet = zauzet; }
    public void setZauzet(boolean zauzet) { this.zauzet = zauzet; }
    public Serviser getServiser() { return serviser; }
    public void setServiser(Serviser serviser) { this.serviser = serviser; }
}