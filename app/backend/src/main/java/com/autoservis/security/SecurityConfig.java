package com.autoservis.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain; // added

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${app.frontend-url}") private String frontendUrl;
  private final OAuth2UserService oAuth2UserService;
  public SecurityConfig(OAuth2UserService svc){ this.oAuth2UserService = svc; }

  @Bean
    SecurityFilterChain filter(HttpSecurity http, OAuth2SuccessHandler successHandler) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/oauth2/**", "/login/**",
                    "/api/auth/user", "/api/auth/logout",
                    "/api/marke/**", "/api/modeli/**",
                    "/api/servis"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(o -> o
                .userInfoEndpoint(ui -> ui.userService(oAuth2UserService))
                .successHandler(successHandler) // << ovdje
                .failureUrl("/?login=fail")
            )
            .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))) // << token auth: map 'uloga' claim to ROLE_* authority
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // << bez server sesije
        return http.build();
    }

    // Map 'uloga' JWT claim -> ROLE_{ULOGA} authority so @PreAuthorize checks work with tokens
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object raw = jwt.getClaim("uloga");
            if (raw == null) return java.util.Collections.emptyList();
            String uloga = raw.toString();
            return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + uloga.toUpperCase()));
        });
        return converter;
    }
}
