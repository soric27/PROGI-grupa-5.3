package com.autoservis.models;

import jakarta.persistence.*;

@Entity
@Table(name = "serviser")
public class Serviser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_serviser")
    private Long idServiser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_osoba", referencedColumnName = "id_osoba")
    private Osoba osoba;

    // TODO: Dodati vezu na Servis kada entitet bude postojao
    // @ManyToOne @JoinColumn(name = "id_servis")
    // private Servis servis;

    @Column(name = "je_li_voditelj")
    private boolean jeLiVoditelj;

    protected Serviser() {}

    public Serviser(Osoba osoba, boolean jeLiVoditelj) {
        this.osoba = osoba;
        this.jeLiVoditelj = jeLiVoditelj;
    }

    // Getteri
    public Long getIdServiser() { return idServiser; }
    public Osoba getOsoba() { return osoba; }
    public boolean isJeLiVoditelj() { return jeLiVoditelj; }
}