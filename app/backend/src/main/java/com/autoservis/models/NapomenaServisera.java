package com.autoservis.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "napomena_servisera")
public class NapomenaServisera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_napomena")
    private Long idNapomena;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prijava")
    private PrijavaServisa prijavaServisa;

    @Column(nullable = false)
    private LocalDateTime datum;

    @Column(nullable = false)
    private String opis;

    protected NapomenaServisera() {}

    public NapomenaServisera(PrijavaServisa prijavaServisa, String opis) {
        this.prijavaServisa = prijavaServisa;
        this.opis = opis;
        this.datum = LocalDateTime.now();
    }
    
    // Getteri
    public Long getIdNapomena() { return idNapomena; }
    public PrijavaServisa getPrijavaServisa() { return prijavaServisa; }
    public LocalDateTime getDatum() { return datum; }
    public String getOpis() { return opis; }
}