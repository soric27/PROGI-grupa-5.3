package com.autoservis.security;

import com.autoservis.models.Osoba;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class SpringOsobaPrincipal implements OAuth2User {
  private final Osoba osoba;
  private final Map<String,Object> attrs;
  private final Collection<? extends GrantedAuthority> auths;

  public SpringOsobaPrincipal(Osoba o, Map<String,Object> attrs, Collection<? extends GrantedAuthority> auths){
    this.osoba=o; this.attrs=attrs; this.auths=auths;
  }

  public Osoba getOsoba(){ return osoba; }
  @Override public Map<String, Object> getAttributes(){ return attrs; }
  @Override public Collection<? extends GrantedAuthority> getAuthorities(){ return auths; }
  @Override public String getName(){ return osoba.getIdOsoba()!=null ? osoba.getIdOsoba().toString() : "unknown"; }
}