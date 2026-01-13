package com.autoservis.security;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.autoservis.models.Osoba;
import com.autoservis.repositories.OsobaRepository;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {
  private final OsobaRepository osobe;
  public OAuth2UserService(OsobaRepository osobe){ this.osobe = osobe; }

  @Override
  public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
    OAuth2User oauth = super.loadUser(req);
    Map<String,Object> a = oauth.getAttributes();

    String oauthId = (String)a.get("sub");
    String email   = (String)a.get("email");
    String ime     = (String)a.getOrDefault("given_name", a.getOrDefault("name","Korisnik"));
    String prezime = (String)a.getOrDefault("family_name","");

    Osoba user = osobe.findByOauthId(oauthId).orElseGet(() -> {
      // default role mapping; certain emails are pre-assigned specific roles
      String defaultRole = "korisnik";
      if ("vitkovicdomagoj@gmail.com".equalsIgnoreCase(email)) {
        defaultRole = "administrator";
      } else if ("katarina.bencun2004@gmail.com".equalsIgnoreCase(email)) {
        defaultRole = "serviser";
      }
      return osobe.save(new Osoba(ime, prezime, email, defaultRole, oauthId));
    });

    // mapiraj uloge na Spring authorities: ROLE_{ULOGA}
    String role = "ROLE_" + user.getUloga().toUpperCase(); // npr. ROLE_ADMINISTRATOR
    var auths = List.of(new SimpleGrantedAuthority(role));

    return new SpringOsobaPrincipal(user, oauth.getAttributes(), auths);
  }
}