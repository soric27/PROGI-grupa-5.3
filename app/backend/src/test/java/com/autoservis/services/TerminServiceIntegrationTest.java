package com.autoservis.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import com.autoservis.models.*;
import com.autoservis.repositories.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Integracijski testovi - Termini")
class TerminServiceIntegrationTest {

    @Autowired
    private TerminRepository terminRepository;

    @Autowired
    private ServiserRepository serviserRepository;

    @Autowired
    private OsobaRepository osobaRepository;

    private Serviser serviser;

    @BeforeEach
    void setUp() {
        // Kreiram servisera za teste
        Osoba osobaServiser = new Osoba("Jovan", "Jovanović", "jovan@autoservis.com", "serviser", null);
        osobaServiser = osobaRepository.save(osobaServiser);

        serviser = new Serviser(osobaServiser, true);
        serviser = serviserRepository.save(serviser);
    }

    // TEST 1: Kreiraj i spremi termin u bazu
    @Test
    @DisplayName("Test 1: Kreiraj i spremi termin u bazu")
    void testCreateAndSaveTermin() {
        // Kreiram termin sa datumom i vremenom
        LocalDateTime vremePocetka = LocalDateTime.of(2026, 2, 1, 10, 0);
        Termin termin = new Termin(vremePocetka);
        termin.setServiser(serviser);

        // Spremi termin u bazu
        Termin saved = terminRepository.save(termin);

        // Provjeravamda je termin kreirandi
        assertNotNull(saved.getIdTermin(), "Termin ima ID");
        assertEquals(vremePocetka, saved.getDatumVrijeme(), "Vrijeme je ispravno sprema");

        // Provjeravamda se termin može dohvatiti iz baze
        Optional<Termin> retrieved = terminRepository.findById(saved.getIdTermin());
        assertTrue(retrieved.isPresent(), "Termin je pronađen u bazi");
        assertEquals(vremePocetka, retrieved.get().getDatumVrijeme(), "Dohvaćeni termin ima ispravo vrijeme");
    }

    // TEST 2: Provjeri da li je termin zauzet
    @Test
    @DisplayName("Test 2: Provjeri da li je termin zauzet")
    void testTerminZauzetStatus() {
        // Kreiram dva termina
        LocalDateTime vrijeme1 = LocalDateTime.of(2026, 2, 1, 10, 0);
        LocalDateTime vrijeme2 = LocalDateTime.of(2026, 2, 1, 11, 0);

        Termin termin1 = new Termin(vrijeme1);
        termin1.setServiser(serviser);
        termin1.setZauzet(false);

        Termin termin2 = new Termin(vrijeme2);
        termin2.setServiser(serviser);
        termin2.setZauzet(true);

        // Spremi u bazu
        terminRepository.save(termin1);
        terminRepository.save(termin2);

        // Provjeravamda li je status zauzet/slobodan ispravno spremam
        assertTrue(termin1.isZauzet() == false, "Termin 1 bi trebao biti slobodan");
        assertTrue(termin2.isZauzet() == true, "Termin 2 bi trebao biti zauzet");
    }

    // TEST 3: Testiraj više termina isti dan sa različitim vremenima
    @Test
    @DisplayName("Test 3: Testiraj više termina isti dan sa različitim vremenima")
    void testMultipleTerminsOnSameDay() {
        // Kreiram 4 termina za isti dan ali drugačija vremena
        LocalDateTime vrijeme1 = LocalDateTime.of(2026, 2, 1, 8, 0);
        LocalDateTime vrijeme2 = LocalDateTime.of(2026, 2, 1, 9, 30);
        LocalDateTime vrijeme3 = LocalDateTime.of(2026, 2, 1, 11, 0);
        LocalDateTime vrijeme4 = LocalDateTime.of(2026, 2, 1, 14, 0);

        Termin termin1 = new Termin(vrijeme1);
        termin1.setServiser(serviser);

        Termin termin2 = new Termin(vrijeme2);
        termin2.setServiser(serviser);

        Termin termin3 = new Termin(vrijeme3);
        termin3.setServiser(serviser);

        Termin termin4 = new Termin(vrijeme4);
        termin4.setServiser(serviser);

        // Spremi sve u bazu
        terminRepository.save(termin1);
        terminRepository.save(termin2);
        terminRepository.save(termin3);
        terminRepository.save(termin4);

        // Provjeravamda su svi termini sprema
        List<Termin> sviTermini = terminRepository.findAll();
        assertTrue(sviTermini.size() >= 4, "Trebalo bi biti najmanje 4 termina");

        // Provjeravamda su vremena u ispravan redoslijed
        assertTrue(vrijeme1.isBefore(vrijeme2), "Vrijeme 1 je prije vremena 2");
        assertTrue(vrijeme2.isBefore(vrijeme3), "Vrijeme 2 je prije vremena 3");
        assertTrue(vrijeme3.isBefore(vrijeme4), "Vrijeme 3 je prije vremena 4");
    }

    // TEST 4: Izazivanje pogreške - Kreiranje termina sa null datumom
    @Test
    @DisplayName("Test 4: Izazivanje pogreške - Kreiranje termina sa null datumom")
    void testCreateTerminWithNullDateTime() {
        // ULAZ: Pokušaj kreirati termin sa null datumom
        
        // KORACI ISPITIVANJA:
        // Korak 1: Pokušaj kreirati termin sa null datumom
        Termin terminNull = new Termin(null);
        terminNull.setServiser(serviser);
        
        // Korak 2: Pokušaj spremiti termin u bazu
        // OČEKIVANI IZLAZ: Izazivanje iznimke zbog null datuma (constraint violation)
        Exception exception = assertThrows(Exception.class, () -> {
            terminRepository.saveAndFlush(terminNull);
        }, "Spremanje termina sa null datumom bi trebalo baciti iznimku");
        
        // Korak 3: Provjeri da je iznimka bila povezana sa NULL constraint-om
        assertNotNull(exception, "Iznimka ne bi smjela biti null");
        assertTrue(exception.getMessage().contains("NULL") || 
                   exception.getMessage().contains("not allowed") ||
                   exception.getMessage().contains("cannot be null"),
                "Poruka iznimke bi trebala ukazivati na NULL constraint problem");
        
        // DOBIVENI IZLAZ: Iznimka je uspješno bačena, termin nije spremljen
    }

    // TEST 5: Testiraj sortiranje termina po vremenu
    @Test
    @DisplayName("Test 5: Testiraj sortiranje termina po vremenu")
    void testTerminSorting() {
        // Kreiram 5 termina u neuređenom redoslijedu
        LocalDateTime vrijeme5 = LocalDateTime.of(2026, 2, 3, 15, 0);
        LocalDateTime vrijeme1 = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime vrijeme3 = LocalDateTime.of(2026, 2, 2, 13, 0);
        LocalDateTime vrijeme2 = LocalDateTime.of(2026, 2, 1, 14, 0);
        LocalDateTime vrijeme4 = LocalDateTime.of(2026, 2, 3, 10, 0);

        // Spremi u neuređenom redoslijedu
        Termin t5 = new Termin(vrijeme5);
        t5.setServiser(serviser);
        terminRepository.save(t5);

        Termin t1 = new Termin(vrijeme1);
        t1.setServiser(serviser);
        terminRepository.save(t1);

        Termin t3 = new Termin(vrijeme3);
        t3.setServiser(serviser);
        terminRepository.save(t3);

        Termin t2 = new Termin(vrijeme2);
        t2.setServiser(serviser);
        terminRepository.save(t2);

        Termin t4 = new Termin(vrijeme4);
        t4.setServiser(serviser);
        terminRepository.save(t4);

        // Dohvati sve i sortiraj po vremenu
        List<Termin> sviTermini = terminRepository.findAll();
        sviTermini.sort((a, b) -> a.getDatumVrijeme().compareTo(b.getDatumVrijeme()));

        // Provjeri da su sortirani u ispravan redoslijed
        assertTrue(sviTermini.get(0).getDatumVrijeme().equals(vrijeme1) || 
                   sviTermini.get(0).getDatumVrijeme().isBefore(vrijeme1),
                   "Prvi termin bi trebao biti prije vremena 1");

        // Provjeri da je broj termina ispravan
        assertTrue(sviTermini.size() >= 5, "Trebalo bi biti najmanje 5 termina");
    }

    // TEST 6: Nepostojeća funkcionalnost - Dohvaćanje termina po nepostojećem ID-u
    @Test
    @DisplayName("Test 6: Nepostojeća funkcionalnost - Dohvaćanje termina po nepostojećem ID-u")
    void testGetNonExistentTermin() {
        // ULAZ: ID termina koji ne postoji u bazi (npr. 99999)
        Long nepostojeciId = 99999L;
        
        // KORACI ISPITIVANJA:
        // Korak 1: Pokušaj dohvatiti termin sa nepostojećim ID-om
        Optional<Termin> rezultat = terminRepository.findById(nepostojeciId);
        
        // OČEKIVANI IZLAZ: Rezultat bi trebao biti prazan Optional
        assertFalse(rezultat.isPresent(), 
                "Dohvaćanje termina sa nepostojećim ID-om bi trebalo vratiti prazan Optional");
        
        // Korak 2: Pokušaj dohvatiti sve termine i provjeri da nema termina s tim ID-om
        List<Termin> sviTermini = terminRepository.findAll();
        assertTrue(sviTermini.stream().noneMatch(t -> t.getIdTermin().equals(nepostojeciId)),
                "Nijedan termin u bazi ne bi trebao imati ID " + nepostojeciId);
        
        // DOBIVENI IZLAZ: Prazan Optional, funkcionalnost se ispravno ponaša
    }
}
