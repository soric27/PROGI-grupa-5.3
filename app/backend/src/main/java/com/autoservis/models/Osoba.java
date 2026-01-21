package com.autoservis.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

  // Setteri za ažuriranje podataka
  public void setIme(String ime){ this.ime = ime; }
  public void setPrezime(String prezime){ this.prezime = prezime; }
  public void setEmail(String email){ this.email = email; }
  public void setUloga(String uloga){ this.uloga = uloga; }
} 