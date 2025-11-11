package main.java.com.autoservis.repositories;
import main.java.com.autoservis.models.Marka;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MarkaRepository extends JpaRepository<Marka, Long> {}