package com.autoservis.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "obrazac")
public class Obrazac {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_obrazac")
  private Long idObrazac;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_prijava")
  private PrijavaServisa prijava;

  private String tip; // predaja | preuzimanje

  @Column(name = "putanja_pdf")
  private String putanjaPdf;

  @Column(name = "datum_generiranja")
  private LocalDateTime datumGeneriranja;

  protected Obrazac() {}

  public Obrazac(PrijavaServisa prijava, String tip, String putanjaPdf) {
    this.prijava = prijava;
    this.tip = tip;
    this.putanjaPdf = putanjaPdf;
    this.datumGeneriranja = LocalDateTime.now();
  }

  public Long getIdObrazac() { return idObrazac; }
  public PrijavaServisa getPrijava() { return prijava; }
  public String getTip() { return tip; }
  public String getPutanjaPdf() { return putanjaPdf; }
  public LocalDateTime getDatumGeneriranja() { return datumGeneriranja; }
}
