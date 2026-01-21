package com.autoservis.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "kvar")
public class Kvar {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_kvar")
  private Long idKvar;

  private String naziv;
  private String opis;

  protected Kvar() {}

  public Kvar(String naziv, String opis) {
    this.naziv = naziv;
    this.opis = opis;
  }

  public Long getIdKvar() { return idKvar; }
  public String getNaziv() { return naziv; }
  public String getOpis() { return opis; }
}
