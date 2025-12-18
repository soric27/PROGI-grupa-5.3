package com.autoservis.models;

import com.autoservis.models.Marka;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity @Table(name="model")
public class Model {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="id_model")
  private Long idModel;

  private String naziv;

  @JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_marka")
  private Marka marka;

  protected Model() {}
  public Model(String naziv, Marka marka){ this.naziv=naziv; this.marka=marka; }

  public Long getIdModel(){ return idModel; }
  public String getNaziv(){ return naziv; }
  public Marka getMarka(){ return marka; }
}