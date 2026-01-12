package com.autoservis.models;

import jakarta.persistence.*;

@Entity
@Table(name = "zamjena_vozilo")
public class ZamjenaVozilo {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_zamjena")
  private Long idZamjena;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_model")
  private Model model;

  private String registracija;

  private Boolean dostupno = true;

  protected ZamjenaVozilo() {}

  public ZamjenaVozilo(Model model, String registracija) {
    this.model = model;
    this.registracija = registracija;
    this.dostupno = true;
  }

  public Long getIdZamjena() { return idZamjena; }
  public Model getModel() { return model; }
  public String getRegistracija() { return registracija; }
  public Boolean getDostupno() { return dostupno; }
  public void setDostupno(Boolean dostupno) { this.dostupno = dostupno; }
}