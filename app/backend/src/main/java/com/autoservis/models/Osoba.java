package com.autoservis.models;

import jakarta.persistence.*;

@Entity @Table(name="osoba")
public class Osoba {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="id_osoba")
  private Long idOsoba;

  private String ime;
  private String prezime;
  private String email;
  private String uloga; // "korisnik" | "administrator"

  @Column(name="oauth_id")
  private String oauthId;

  protected Osoba() {}
  public Osoba(String ime, String prezime, String email, String uloga, String oauthId){
    this.ime=ime; this.prezime=prezime; this.email=email; this.uloga=uloga; this.oauthId=oauthId;
  }

  public Long getIdOsoba(){ return idOsoba; }
  public String getIme(){ return ime; }
  public String getPrezime(){ return prezime; }
  public String getEmail(){ return email; }
  public String getUloga(){ return uloga; }
  public String getOauthId(){ return oauthId; }
}