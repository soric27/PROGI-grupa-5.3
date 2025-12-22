package com.autoservis.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    // Getteri i Setteri
    public java.util.List<NapomenaServisera> getNapomene() { return napomene; }
    public Long getIdPrijava() { return idPrijava; }
    public Vozilo getVozilo() { return vozilo; }
    public Serviser getServiser() { return serviser; }
    public Termin getTermin() { return termin; }
    public String getStatus() { return status; }
    public LocalDateTime getDatumPrijave() { return datumPrijave; }
    public String getNapomenaVlasnika() { return napomenaVlasnika; }
    
    public void setStatus(String status) { this.status = status; }
    // ... ostali setteri po potrebi
}