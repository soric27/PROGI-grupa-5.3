package com.autoservis.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // omogućava @PreAuthorize
public class SecurityConfig {

  @Value("${app.frontend-url}") private String frontendUrl;
  private final OAuth2UserService oAuth2UserService;
  public SecurityConfig(OAuth2UserService svc){ this.oAuth2UserService = svc; }

  @Bean
  SecurityFilterChain filter(HttpSecurity http) throws Exception {
    http
      .cors(Customizer.withDefaults())
      .csrf(csrf -> csrf.disable()) // kompatibilno s postojećim SPA-om
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/marke/**","/api/modeli/**").permitAll()
        .anyRequest().authenticated()
      )
      .oauth2Login(o -> o
        .loginPage("/api/auth/login/google")
        .userInfoEndpoint(ui -> ui.userService(oAuth2UserService))
        .defaultSuccessUrl(frontendUrl + "?login=success", true)
        .failureUrl(frontendUrl + "?login=fail")
      )
      .logout(lo -> lo
        .logoutUrl("/api/auth/logout")
        .logoutSuccessHandler((req,res,auth) -> res.sendRedirect(frontendUrl))
      )
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
    return http.build();
  }
}