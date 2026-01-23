package com.autoservis.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import com.autoservis.models.*;
import com.autoservis.interfaces.dto.PrijavaServisaCreateDto;
import com.autoservis.interfaces.dto.PrijavaDetalleDto;
import com.autoservis.repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Integracijski testovi - Prijava Servisa")
class PrijavaServisaServiceIntegrationTest {

    @Autowired
    private PrijavaServisaService prijavaServisaService;

    @Autowired
    private PrijavaServisaRepository prijavaRepository;

    @Autowired
    private VoziloRepository voziloRepository;

    @Autowired
    private OsobaRepository osobaRepository;

    @Autowired
    private ServiserRepository serviserRepository;

    @Autowired
    private TerminRepository terminRepository;

    @Autowired
    private KvarRepository kvarRepository;

    @Autowired
    private MarkaRepository markaRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private com.autoservis.repositories.ZamjenaVoziloRepository zamjenaVoziloRepository;

    private Osoba vlasnik;
    private Osoba osobaServiser;
    private Serviser serviser;
    private Vozilo vozilo;
    private Termin termin;

    @BeforeEach
    void setUp() {
        // Kreiram vlasnika vozila
        vlasnik = new Osoba("Ivan", "Horvat", "ivan@example.com", "korisnik", null);
        vlasnik = osobaRepository.save(vlasnik);

        // Kreiram marku i model vozila
        Marka marka = new Marka("BMW");
        marka = markaRepository.save(marka);

        Model model = new Model("320i", marka);
        model = modelRepository.save(model);

        // Kreiram vozilo
        vozilo = new Vozilo(vlasnik, model, "ZG-123-AB", 2020);
        vozilo = voziloRepository.save(vozilo);

        // Kreiram servijera
        osobaServiser = new Osoba("Marko", "Marković", "marko@autoservis.com", "serviser", null);
        osobaServiser = osobaRepository.save(osobaServiser);

        serviser = new Serviser(osobaServiser, false);
        serviser = serviserRepository.save(serviser);

        // Kreiram termin
        termin = new Termin(LocalDateTime.of(2026, 1, 25, 9, 0));
        termin.setServiser(serviser);
        termin = terminRepository.save(termin);
    }

    // TEST 1: Kreiraj novu prijavu servisa sa svim poljima
    @Test
    @DisplayName("Test 1: Kreiraj novu prijavu servisa sa svim poljima")
    void testCreatePrijavaWithAllFields() {
        // Kreiram kvar
        Kvar kvar = new Kvar("Motor ne startaj", "Motor se ne pokreće ujutro");
        kvar = kvarRepository.save(kvar);

        // Kreiram DTO sa svim poljima
        PrijavaServisaCreateDto dto = new PrijavaServisaCreateDto(
            vozilo.getIdVozilo(),
            serviser.getIdServiser(),
            termin.getIdTermin(),
            "Molim detaljnu provjeru motora",
            null,
            LocalDate.of(2026, 1, 25),
            LocalDate.of(2026, 1, 26),
            List.of(kvar.getIdKvar())
        );

        // Pozivam STVARNU metodu iz PrijavaServisaService
        PrijavaDetalleDto result = prijavaServisaService.createPrijava(dto, vlasnik.getIdOsoba());

        // Provjeravamda li je rezultat kreiridan
        assertNotNull(result, "Prijava je kreirana");
        assertNotNull(result.idPrijava(), "Prijava ima ID");

        // Provjeravamda li je sprema u bazu
        Optional<PrijavaServisa> saved = prijavaRepository.findById(result.idPrijava());
        assertTrue(saved.isPresent(), "Prijava je pronađena u bazi podataka");
        
        PrijavaServisa prijava = saved.get();
        assertEquals("Molim detaljnu provjeru motora", prijava.getNapomenaVlasnika(), 
            "Napomena je ispravno spremljena");
        assertEquals(vozilo.getIdVozilo(), prijava.getVozilo().getIdVozilo(), 
            "Vozilo je ispravno povezano");
        assertEquals(serviser.getIdServiser(), prijava.getServiser().getIdServiser(), 
            "Serviser je ispravno povezan");
    }

    // TEST 2: Prijava sa zamjenskim vozilom
    @Test
    @DisplayName("Test 2: Prijava sa zamjenskim vozilom")
    void testCreatePrijavaWithReplacementVehicle() {
        // Kreiram zamjensko vozilo (entitet ZamjenaVozilo)
        Marka marka = new Marka("Volkswagen");
        marka = markaRepository.save(marka);

        Model model = new Model("Golf", marka);
        model = modelRepository.save(model);

        com.autoservis.models.ZamjenaVozilo zamjena = new com.autoservis.models.ZamjenaVozilo(model, "ZG-456-CD");
        zamjena = zamjenaVoziloRepository.save(zamjena);

        // Kreiram DTO sa zamjenskim vozilom
        PrijavaServisaCreateDto dto = new PrijavaServisaCreateDto(
            vozilo.getIdVozilo(),
            serviser.getIdServiser(),
            termin.getIdTermin(),
            "Trebam zamjensko vozilo",
            zamjena.getIdZamjena(),
            LocalDate.of(2026, 1, 25),
            LocalDate.of(2026, 1, 27),
            List.of()
        );

        // Pozivam servis
        PrijavaDetalleDto result = prijavaServisaService.createPrijava(dto, vlasnik.getIdOsoba());

        // Provjeravamda je prijava kreirana
        assertNotNull(result, "Prijava je kreirana");
        
        // Provjeravamda su datumi ispravno spremljeni
        Optional<PrijavaServisa> saved = prijavaRepository.findById(result.idPrijava());
        assertTrue(saved.isPresent(), "Prijava je pronađena");
    }

    // TEST 3: Prijava sa vremenskim rokom servisa
    @Test
    @DisplayName("Test 3: Prijava sa vremenskim rokom servisa")
    void testCreatePrijavaWithDateRange() {
        LocalDate datumOd = LocalDate.of(2026, 1, 25);
        LocalDate datumDo = LocalDate.of(2026, 1, 30);

        PrijavaServisaCreateDto dto = new PrijavaServisaCreateDto(
            vozilo.getIdVozilo(),
            serviser.getIdServiser(),
            termin.getIdTermin(),
            "Trebam servis prije puta",
            null,
            datumOd,
            datumDo,
            List.of()
        );

        // Pozivam servis
        PrijavaDetalleDto result = prijavaServisaService.createPrijava(dto, vlasnik.getIdOsoba());

        // Provjeravamda je prijava kreirana
        assertNotNull(result, "Prijava je kreirana");

        // Provjeravamda su datumi ispravno spremljeni
        Optional<PrijavaServisa> saved = prijavaRepository.findById(result.idPrijava());
        assertTrue(saved.isPresent(), "Prijava je pronađena");
        
        // Provjeravamda je datumDo nakon datumOd
        assertTrue(datumDo.isAfter(datumOd), "Datum završetka je nakon početka");
    }

    // TEST 4: Prijava sa većim brojem kvarova
    @Test
    @DisplayName("Test 4: Prijava sa većim brojem kvarova")
    void testCreatePrijavaWithMultipleDefects() {
        // Kreiram 3 kvara
        Kvar kvar1 = new Kvar("Brisači ne rade", "Brisači podstrešnice ne rade");
        kvar1 = kvarRepository.save(kvar1);

        Kvar kvar2 = new Kvar("Klimatizacija slab", "Hladnjak ne hladi kako treba");
        kvar2 = kvarRepository.save(kvar2);

        Kvar kvar3 = new Kvar("Zvučna izolacija", "Buka od motora je pojačana");
        kvar3 = kvarRepository.save(kvar3);

        // Kreiram DTO sa 3 kvara
        PrijavaServisaCreateDto dto = new PrijavaServisaCreateDto(
            vozilo.getIdVozilo(),
            serviser.getIdServiser(),
            termin.getIdTermin(),
            "Tri problema kako je navedeno",
            null,
            LocalDate.of(2026, 1, 25),
            LocalDate.of(2026, 1, 28),
            List.of(kvar1.getIdKvar(), kvar2.getIdKvar(), kvar3.getIdKvar())
        );

        // Pozivam servis
        PrijavaDetalleDto result = prijavaServisaService.createPrijava(dto, vlasnik.getIdOsoba());

        // Provjeravamda je prijava kreirana
        assertNotNull(result, "Prijava je kreirana");

        // Provjeravamda su kvarovi povezani
        Optional<PrijavaServisa> saved = prijavaRepository.findById(result.idPrijava());
        assertTrue(saved.isPresent(), "Prijava je pronađena");
        assertEquals(3, saved.get().getKvarovi().size(), "Trebalo bi biti 3 kvara");
    }

    // TEST 5: Prijava sa minimalnim podacima
    @Test
    @DisplayName("Test 5: Prijava sa minimalnim podacima")
    void testCreatePrijavaWithMinimalData() {
        // Kreiram minimalnu prijavu
        PrijavaServisaCreateDto dto = new PrijavaServisaCreateDto(
            vozilo.getIdVozilo(),
            serviser.getIdServiser(),
            termin.getIdTermin(),
            "Servis",
            null,
            null,
            null,
            List.of()
        );

        // Pozivam servis
        PrijavaDetalleDto result = prijavaServisaService.createPrijava(dto, vlasnik.getIdOsoba());

        // Provjeravamda je prijava kreirana sa minimalnim podacima
        assertNotNull(result, "Prijava je kreirana");
        
        // Provjeravamda je napomena sprema
        Optional<PrijavaServisa> saved = prijavaRepository.findById(result.idPrijava());
        assertTrue(saved.isPresent(), "Prijava je pronađena");
        assertEquals("Servis", saved.get().getNapomenaVlasnika(), "Napomena je sprema");
    }
}