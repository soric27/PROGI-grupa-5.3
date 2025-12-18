package com.autoservis.repositories;
import com.autoservis.models.Osoba;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OsobaRepository extends JpaRepository<Osoba, Long> {
  Optional<Osoba> findByOauthId(String oauthId);
  Optional<Osoba> findByEmail(String email);
}