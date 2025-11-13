package com.autoservis.models;

import jakarta.persistence.*;

@Entity @Table(name="marka")
public class Marka {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="id_marka")
  private Long idMarka;

  private String naziv;

  protected Marka() {}
  public Marka(String naziv){ this.naziv = naziv; }

  public Long getIdMarka(){ return idMarka; }
  public String getNaziv(){ return naziv; }
}