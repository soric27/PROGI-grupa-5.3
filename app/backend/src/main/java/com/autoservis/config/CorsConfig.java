package com.autoservis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
public class CorsConfig {
  @Value("${app.frontend-url:http://localhost:3000}")
  private String frontend;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of(
    frontend, // iz env varijable, npr. https://progi-grupa-5-3-vnly.onrender.com
    "http://localhost:3000"
));

    cfg.setAllowedMethods(List.of("GET","POST","PATCH","PUT","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setAllowCredentials(true);

    cfg.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/**", cfg);
    return src;
  }
}