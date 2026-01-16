package com.autoservis.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "prijavaServisa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NapomenaServisera> napomene = new ArrayList<>();

    protected PrijavaServisa() {}

    public PrijavaServisa(Vozilo vozilo, Serviser serviser, Termin termin, String napomenaVlasnika) {
        this.vozilo = vozilo;
        this.serviser = serviser;
        this.termin = termin;
        this.napomenaVlasnika = napomenaVlasnika;
        this.status = "zaprimljeno";
        this.datumPrijave = LocalDateTime.now();
    }

    public Long getIdPrijava() { return idPrijava; }
    public Vozilo getVozilo() { return vozilo; }
    public Serviser getServiser() { return serviser; }
    public Termin getTermin() { return termin; }
    public String getStatus() { return status; }
    public LocalDateTime getDatumPrijave() { return datumPrijave; }
    public LocalDateTime getDatumPredaje() { return datumPredaje; }
    public LocalDateTime getDatumPreuzimanja() { return datumPreuzimanja; }
    public String getNapomenaVlasnika() { return napomenaVlasnika; }
    public List<NapomenaServisera> getNapomene() { return napomene; }

    public void setVozilo(Vozilo vozilo) { this.vozilo = vozilo; }
    public void setServiser(Serviser serviser) { this.serviser = serviser; }
    public void setTermin(Termin termin) { this.termin = termin; }
    public void setStatus(String status) { this.status = status; }
    public void setDatumPredaje(LocalDateTime datumPredaje) { this.datumPredaje = datumPredaje; }
    public void setDatumPreuzimanja(LocalDateTime datumPreuzimanja) { this.datumPreuzimanja = datumPreuzimanja; }
    public void setNapomenaVlasnika(String napomenaVlasnika) { this.napomenaVlasnika = napomenaVlasnika; }
}
