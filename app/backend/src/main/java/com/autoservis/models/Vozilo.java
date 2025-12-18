package com.autoservis.models;

import com.autoservis.models.Model;
import com.autoservis.models.Osoba;
import jakarta.persistence.*;

@Entity @Table(name="vozilo")
public class Vozilo {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="id_vozilo")
  private Long idVozilo;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_osoba")
  private Osoba osoba;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="id_model")
  private Model model;

  private String registracija;

  @Column(name="godina_proizvodnje")
  private Integer godinaProizvodnje;

  protected Vozilo(){}

  public Vozilo(Osoba osoba, Model model, String registracija, Integer godinaProizvodnje){
    this.osoba=osoba; this.model=model; this.registracija=registracija; this.godinaProizvodnje=godinaProizvodnje;
  }

  public Long getIdVozilo(){ return idVozilo; }
  public Osoba getOsoba(){ return osoba; }
  public Model getModel(){ return model; }
  public String getRegistracija(){ return registracija; }
  public Integer getGodinaProizvodnje(){ return godinaProizvodnje; }
}