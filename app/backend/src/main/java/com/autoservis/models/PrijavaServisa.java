package com.autoservis.models;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "prijava_servisa")
public class PrijavaServisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prijava")
    private Long idPrijava;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vozilo")
    private Vozilo vozilo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_serviser")
    private Serviser serviser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_termin")
    private Termin termin;

    @Column(nullable = false)
    private String status;

    @Column(name = "datum_prijave", nullable = false)
    private LocalDateTime datumPrijave;

    @Column(name = "datum_predaje")
    private LocalDateTime datumPredaje;

    @Column(name = "datum_preuzimanja")
    private LocalDateTime datumPreuzimanja;

    @Column(name = "napomena_vlasnika")
    private String napomenaVlasnika;
    
    protected PrijavaServisa() {}

    public PrijavaServisa(Vozilo vozilo, Serviser serviser, Termin termin, String napomenaVlasnika) {
        this.vozilo = vozilo;
        this.serviser = serviser;
        this.termin = termin;
        this.napomenaVlasnika = napomenaVlasnika;
        this.status = "zaprimljeno";
        this.datumPrijave = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "prijavaServisa", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<NapomenaServisera> napomene = new java.util.ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @jakarta.persistence.JoinTable(
        name = "prijava_kvar",
        joinColumns = @JoinColumn(name = "id_prijava"),
        inverseJoinColumns = @JoinColumn(name = "id_kvar")
    )
    private java.util.List<Kvar> kvarovi = new java.util.ArrayList<>();

    // Getteri i Setteri
    public java.util.List<NapomenaServisera> getNapomene() { return napomene; }
    public java.util.List<Kvar> getKvarovi() { return kvarovi; }
    public void setKvarovi(java.util.List<Kvar> kvarovi) { this.kvarovi = kvarovi; }
    public Long getIdPrijava() { return idPrijava; }
    public Vozilo getVozilo() { return vozilo; }
    public Serviser getServiser() { return serviser; }
    public Termin getTermin() { return termin; }
    public String getStatus() { return status; }
    public LocalDateTime getDatumPrijave() { return datumPrijave; }
    public String getNapomenaVlasnika() { return napomenaVlasnika; }
    
    public void setStatus(String status) { this.status = status; }

    // getters for dates used by stats
    public LocalDateTime getDatumPredaje() { return datumPredaje; }
    public LocalDateTime getDatumPreuzimanja() { return datumPreuzimanja; }

    // setter/getter for termin to support updates
    public void setTermin(Termin termin) { this.termin = termin; }
    public void setDatumPredaje(LocalDateTime dt) { this.datumPredaje = dt; }
    public void setDatumPreuzimanja(LocalDateTime dt) { this.datumPreuzimanja = dt; }

    // allow updating vehicle when authorized
    public void setVozilo(Vozilo vozilo) { this.vozilo = vozilo; }

    // ... ostali setteri po potrebi
}