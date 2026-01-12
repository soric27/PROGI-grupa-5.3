package com.autoservis.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rezervacija_zamjene")
public class RezervacijaZamjene {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_rezervacija")
  private Long idRezervacija;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_prijava")
  private PrijavaServisa prijava;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_zamjena")
  private ZamjenaVozilo zamjena;

  @Column(name = "datum_od")
  private LocalDate datumOd;

  @Column(name = "datum_do")
  private LocalDate datumDo;

  protected RezervacijaZamjene() {}

  public RezervacijaZamjene(PrijavaServisa prijava, ZamjenaVozilo zamjena, LocalDate datumOd, LocalDate datumDo) {
    this.prijava = prijava;
    this.zamjena = zamjena;
    this.datumOd = datumOd;
    this.datumDo = datumDo;
  }

  public Long getIdRezervacija() { return idRezervacija; }
  public PrijavaServisa getPrijava() { return prijava; }
  public ZamjenaVozilo getZamjena() { return zamjena; }
  public LocalDate getDatumOd() { return datumOd; }
  public LocalDate getDatumDo() { return datumDo; }
}
