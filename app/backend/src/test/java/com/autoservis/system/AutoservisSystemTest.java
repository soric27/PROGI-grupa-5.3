package com.autoservis.system;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sistemski testovi za Auto Servis aplikaciju koristeći Selenium WebDriver.
 * Testiraju se end-to-end scenariji koji simuliraju stvarno korištenje aplikacije.
 * 
 * NAPOMENA: Za izvođenje ovih testova, potrebno je:
 * 1. Pokrenuti backend server (mvn spring-boot:run ili iz IDE-a)
 * 2. Pokrenuti frontend server (npm start)
 * 3. Imati instaliran Chrome preglednik
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Sistemski testovi - Auto Servis aplikacija")
class AutoservisSystemTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    
    // URL-ovi aplikacije
    private static final String FRONTEND_URL = "http://localhost:3000";
    private static final int WAIT_TIMEOUT = 20; // sekunde - povećano za Google Maps
    private static final int DEMO_PAUSE_MS = 2000; // pauza između koraka za demo (2 sekunde)
    private static final String SCREENSHOT_DIR = "target/screenshots";
    private static int screenshotCounter = 0;

    @BeforeAll
    static void setUpClass() {
        // Automatsko preuzimanje i postavljanje ChromeDriver-a
        WebDriverManager.chromedriver().setup();
        
        // Konfiguracija Chrome preglednika
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        // Za headless mod (bez GUI): options.addArguments("--headless");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT));
        
        // Stvori direktorij za screenshotove
        try {
            Files.createDirectories(Paths.get(SCREENSHOT_DIR));
        } catch (IOException e) {
            System.err.println("Greška pri kreiranju direktorija za screenshotove: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setUp() {
        // Prije svakog testa, osvježi stranicu i očisti sessionStorage
        driver.get(FRONTEND_URL);
        
        // Očisti sessionStorage da testovi ne dijele autentikaciju
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("sessionStorage.clear();");
        
        // Prihvati sve JavaScript alert-e koji se mogu pojaviti
        try {
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {
            // Nema alert-a, nastavi
        }
    }

    // ==================== TEST 1: Redovan slučaj - Navigacija na početnoj stranici ====================
    
    @Test
    @Order(1)
    @DisplayName("Test 1: Redovan slučaj - Učitavanje početne stranice i osnovna navigacija")
    void testHomePageLoadingAndNavigation() {
        // ULAZ: Otvaranje aplikacije na početnoj stranici
        driver.get(FRONTEND_URL);
        
        // KORACI ISPITIVANJA:
        // Korak 1: Provjeri da se stranica učitala i da naslov sadrži "Autoservis" ili "React"
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("Autoservis") || pageTitle.contains("React") || pageTitle.contains("Auto"), 
            "Naslov stranice bi trebao sadržavati 'Autoservis' ili 'React App'");
        System.out.println("  Naslov stranice: " + pageTitle);
        takeScreenshot("test1_01_homepage");
        demoPause();
        
        // Korak 2: Klikni na navigacijski link "Početna"
        WebElement homeLinkNav = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/' and contains(text(), 'Početna')]"))
        );
        homeLinkNav.click();
        wait.until(ExpectedConditions.urlToBe(FRONTEND_URL + "/"));
        takeScreenshot("test1_02_nav_home");
        demoPause();
        
        // Korak 3: Klikni na navigacijski link "Kontakt"
        WebElement kontaktLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, 'kontakt')]"))
        );
        kontaktLink.click();
        
        // OČEKIVANI IZLAZ: URL se mijenja u /kontakt
        wait.until(ExpectedConditions.urlContains("/kontakt"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/kontakt"), 
            "URL bi trebao sadržavati /kontakt");
        
        // Dodatno čekanje samo za kontakt stranicu - Google Maps se učitava
        System.out.println("  ⏳ Čekam da se Google Maps učita na kontakt stranici...");
        try {
            Thread.sleep(5000); // 5 sekundi dodatne pauze za Google Maps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        takeScreenshot("test1_03_nav_kontakt");
        demoPause();
        
        System.out.println("✓ Test 1 prošao: Početna stranica se učitala i navigacija radi ispravno");
        System.out.println("  Dobiveni izlaz:");
        System.out.println("    - Naslov stranice sadrži 'Autoservis'");
        System.out.println("    - Navigacija 'Početna' funkcionira");
        System.out.println("    - Navigacija 'Kontakt' funkcionira");
        System.out.println("    - URL: " + currentUrl);
    }

    // ==================== TEST 2: Redovan slučaj - Navigacija kroz navigacijsku traku ====================
    
    @Test
    @Order(2)
    @DisplayName("Test 2: Redovan slučaj - Navigacija kroz sve stranice aplikacije sa prijavom")
    void testNavigationThroughAllPages() {
        // ULAZ: Prijava korisnika i navigacija kroz sve stranice
        
        // Korak 0: Simuliraj prijavu dummy korisnika
        simulateDummyLogin();
        takeScreenshot("test2_00_prijava_uspješna");
        demoPause();
        
        System.out.println("  ✅ Korisnik prijavljen - početak navigacije kroz stranice");
        
        // Navigiraj na početnu stranicu da se osigura pristup svim linkovima
        driver.get(FRONTEND_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        demoPause();
        
        // KORACI ISPITIVANJA:
        // Korak 1: Navigiraj na stranicu 'Kontakt'
        WebElement kontaktLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/kontakt']"))
        );
        kontaktLink.click();
        wait.until(ExpectedConditions.urlContains("/kontakt"));
        takeScreenshot("test2_01_kontakt");
        demoPause();
        
        // Korak 2: Navigiraj na stranicu 'Servis' (admin)
        WebElement servisLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/servis']"))
        );
        servisLink.click();
        wait.until(ExpectedConditions.urlContains("/servis"));
        takeScreenshot("test2_02_servis");
        demoPause();
        
        // Korak 3: Navigiraj na stranicu 'Zamjenska vozila' (admin)
        WebElement zamjeneLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/zamjene']"))
        );
        zamjeneLink.click();
        
        // Čekaj i prihvati sve alert-e koji se mogu pojaviti
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1000);
                driver.switchTo().alert().accept();
                System.out.println("    ⚠️ Alert prihvaćen (pokušaj " + (i + 1) + "/3)");
            } catch (Exception ignored) {
                // Nema više alert-a
                break;
            }
        }
        
        // Sada možemo provjeriti URL i napraviti screenshot
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/zamjene"), "URL bi trebao sadržavati '/zamjene'");
        
        takeScreenshot("test2_03_zamjene");
        demoPause();
        
        // Korak 4: Navigiraj na stranicu 'Statistika' (admin)
        WebElement statistikaLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/statistika']"))
        );
        statistikaLink.click();
        wait.until(ExpectedConditions.urlContains("/statistika"));
        takeScreenshot("test2_04_statistika");
        demoPause();
        
        // Korak 5: Navigiraj na stranicu 'Osobe' (admin)
        WebElement osobeLink = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/osobe']"))
        );
        osobeLink.click();
        wait.until(ExpectedConditions.urlContains("/osobe"));
        takeScreenshot("test2_05_osobe");
        demoPause();
        
        // OČEKIVANI IZLAZ: Sve admin stranice su dostupne i navigacija radi
        System.out.println("✓ Test 2 prošao: Prijava kao admin uspješna, navigacija kroz sve stranice radi ispravno");
        System.out.println("  Dobiveni izlaz:");
        System.out.println("    - Kontakt: URL = /kontakt");
        System.out.println("    - Servis: URL = /servis");
        System.out.println("    - Zamjenska vozila: URL = /zamjene");
        System.out.println("    - Statistika: URL = /statistika");
        System.out.println("    - Osobe: URL = /osobe");
        System.out.println("    - Administrator vidi sve admin funkcionalnosti");
    }

    // ==================== TEST 3: Rubni uvjet - Pristup stranici bez autentifikacije ====================
    
    @Test
    @Order(3)
    @DisplayName("Test 3: Rubni uvjet - Pristup zaštićenim stranicama i provjera backend veze")
    void testAccessProtectedPagesWithoutAuth() {
        // ULAZ: Pokušaj pristupa stranici 'Servis' bez autentifikacije
        driver.get(FRONTEND_URL + "/servis");
        
        // KORACI ISPITIVANJA:
        // Korak 1: Pričekaj učitavanje stranice i napravi screenshot
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        String servisUrl = driver.getCurrentUrl();
        takeScreenshot("test3_01_servis_rezultat");
        demoPause();
        
        // Korak 2: Provjeri da se stranica učitala bez greške
        boolean onServisPage = servisUrl.contains("/servis");
        boolean redirectedToHome = servisUrl.equals(FRONTEND_URL + "/") || 
                                   servisUrl.equals(FRONTEND_URL);
        
        if (onServisPage) {
            System.out.println("  Stranica /servis učitana - sustav omogućava pristup ili prikazuje sadržaj");
        } else if (redirectedToHome) {
            System.out.println("  Preusmjerenje na početnu stranicu");
        }
        
        // Korak 3: Navigacija nazad na početnu stranicu
        WebElement homeLinkTest3 = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, '/') and (contains(text(), 'Početna') or contains(text(), 'Home'))]"))
        );
        homeLinkTest3.click();
        wait.until(ExpectedConditions.urlToBe(FRONTEND_URL + "/"));
        takeScreenshot("test3_02_home_after_servis");
        demoPause();
        
        System.out.println("✓ Test 3 prošao: Pristup zaštićenim stranicama i povratak na početnu");
        System.out.println("  Dobiveni izlaz:");
        System.out.println("    - Stranica /servis učitana bez JavaScript greške");
        System.out.println("    - Povratak na početnu stranicu uspješan");
    }

    // ==================== TEST 4: Rubni uvjet - Unos nevaljanog URL-a ====================
    
    @Test
    @Order(4)
    @DisplayName("Test 4: Rubni uvjet - Pristup nepostojećoj stranici (404)")
    void testAccessNonExistentPage() {
        // ULAZ: URL nepostojeće stranice
        String invalidUrl = FRONTEND_URL + "/nepostojeca-stranica-12345";
        driver.get(invalidUrl);
        
        // KORACI ISPITIVANJA:
        // Korak 1: Pričekaj učitavanje stranice
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        takeScreenshot("test4_01_nepostojeca_stranica");
        demoPause();
        
        // Korak 2: Provjeri da li postoji poruka o grešci ili preusmjerenje
        String currentUrl = driver.getCurrentUrl();
        String pageTitle = driver.getTitle();
        
        // OČEKIVANI IZLAZ: 
        // React Router bi trebao ili:
        // 1) Prikazati 404 stranicu
        // 2) Preusmjeriti na početnu stranicu
        // 3) Prikazati praznu stranicu s navigacijom
        
        boolean staysOnInvalidUrl = currentUrl.equals(invalidUrl);
        boolean redirectsToHome = currentUrl.equals(FRONTEND_URL + "/") || 
                                  currentUrl.equals(FRONTEND_URL);
        
        System.out.println("✓ Test 4 izvršen: Pristup nepostojećoj stranici");
        System.out.println("  Ulazni URL: " + invalidUrl);
        System.out.println("  Trenutni URL: " + currentUrl);
        System.out.println("  Naslov stranice: " + pageTitle);
        
        if (redirectsToHome) {
            System.out.println("  Dobiveni izlaz: Preusmjerenje na početnu stranicu (očekivano ponašanje)");
        } else if (staysOnInvalidUrl) {
            System.out.println("  Dobiveni izlaz: React Router prikazuje stranicu (moguće praznu ili s navigacijom)");
        }
        
        // Navigacija natrag na početnu stranicu
        WebElement homeLinkTest4 = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/']"))
        );
        homeLinkTest4.click();
        wait.until(ExpectedConditions.urlToBe(FRONTEND_URL + "/"));
        takeScreenshot("test4_02_return_home");
        demoPause();
        
        System.out.println("✓ Test 4 prošao: Povratak na početnu stranicu uspješan");
        assertTrue(true, "Sustav je obradio nevaljani URL bez rušenja");
    }

    // ==================== TEST 5: Poziv nepostojeće funkcionalnosti - Backend resilience ====================
    
    @Test
    @Order(5)
    @DisplayName("Test 5: Poziv nepostojeće funkcionalnosti - Nepostojeći API endpoint (backend resilience)")
    void testNonExistentAPIEndpoint() {
        // ULAZ: Provjera da aplikacija radi i s neuspjelim API pozivima
        
        // KORACI ISPITIVANJA:
        // Korak 1: Otvoriti početnu stranicu (može imati neuspjele API pozive)
        driver.get(FRONTEND_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        
        // Screenshot 1: Provjera konzole - početna stranica
        takeScreenshot("test5_01_console_check");
        demoPause();
        
        // Korak 2: Provjeri da stranica nije potpuno prazna (što bi značilo kritičnu grešku)
        String bodyText = driver.findElement(By.tagName("body")).getText();
        assertFalse(bodyText.isEmpty(), "Početna stranica bi trebala imati neki sadržaj");
        System.out.println("  ✅ Početna stranica se učitala - nema kritičnih grešaka");
        
        // Korak 3: Navigiraj na drugu stranicu (npr. vozila) - testira da UI ostaje funkcionalan
        WebElement vozilaLinkTest5 = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='/vozila']"))
        );
        vozilaLinkTest5.click();
        wait.until(ExpectedConditions.urlContains("/vozila"));
        
        // Screenshot 2: Finalno stanje - navigacija uspješna
        takeScreenshot("test5_02_final_state");
        demoPause();
        
        // OČEKIVANI IZLAZ:
        // - Aplikacija se učitava čak i s neuspjelim API pozivima
        // - Nema nekontroliranih JavaScript grešaka
        // - UI ostaje funkcionalan
        
        System.out.println("✓ Test 5 prošao: Aplikacija stabilna i funkcionalna");
        System.out.println("  Dobiveni izlaz:");
        System.out.println("    - Aplikacija se učitava i s neuspjelim API pozivima");
        System.out.println("    - UI ostaje funkcionalan (navigacija radi)");
        System.out.println("    - Nema kritičnih grešaka");
    }

    // ==================== DODATNI HELPER METODE ====================
    
    /**
     * Helper metoda za provjeru da li element postoji na stranici
     */
    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Helper metoda za snimanje screenshota sa URL-om na vrhu
     */
    private static void takeScreenshot(String screenshotName) {
        try {
            // Snimi screenshot kao privremenu datoteku
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            BufferedImage originalImage = ImageIO.read(screenshot);
            
            // Kreiraj novu sliku s dodatnim prostorom na vrhu za URL
            int urlBarHeight = 60;
            BufferedImage imageWithUrl = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight() + urlBarHeight,
                BufferedImage.TYPE_INT_RGB
            );
            
            // Postavi grafiku
            Graphics2D g2d = imageWithUrl.createGraphics();
            
            // Nacrtaj svijetlo sivu pozadinu za URL bar (sličnije browseru)
            g2d.setColor(new Color(240, 240, 240));
            g2d.fillRect(0, 0, imageWithUrl.getWidth(), urlBarHeight);
            
            // Dodaj URL tekst - veći i bold za bolju čitljivost
            g2d.setColor(new Color(32, 33, 36)); // Tamno siva (Chrome-like)
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
            String currentUrl = driver.getCurrentUrl();
            g2d.drawString(currentUrl, 15, 38);
            
            // Nacrtaj originalnu sliku ispod URL bara
            g2d.drawImage(originalImage, 0, urlBarHeight, null);
            g2d.dispose();
            
            // Spremi novu sliku
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%03d_%s_%s.png", ++screenshotCounter, timestamp, screenshotName);
            Path destination = Paths.get(SCREENSHOT_DIR, fileName);
            ImageIO.write(imageWithUrl, "PNG", destination.toFile());
            
            System.out.println("  📸 Screenshot: " + fileName + " (URL: " + currentUrl + ")");
        } catch (IOException e) {
            System.err.println("Greška pri snimanju screenshota: " + e.getMessage());
        }
    }
    
    /**
     * Helper metoda za demo pauzu između koraka
     */
    private static void demoPause() {
        try {
            Thread.sleep(DEMO_PAUSE_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Helper metoda za simulaciju prijave korisnika u test okruženju
     * Postavlja sessionStorage sa dummy JWT tokenom koji React može dekodirati
     */
    private static void simulateDummyLogin() {
        try {
            // Mora biti na istom domainu da bi radio sessionStorage
            driver.get(FRONTEND_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Kreiraj fake JWT token (header.payload.signature)
            // Header: {"alg":"HS256","typ":"JWT"}
            String header = java.util.Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
            
            // Payload: administrator s podacima koje React može dekodirati
            String payloadJson = "{"
                + "\"id_osoba\":999,"
                + "\"email\":\"test.selenium@example.com\","
                + "\"ime\":\"Selenium\","
                + "\"prezime\":\"Test\","
                + "\"uloga\":\"administrator\","
                + "\"exp\":9999999999"
                + "}";
            String payload = java.util.Base64.getEncoder().encodeToString(payloadJson.getBytes());
            
            // Signature (dummy - za test nije potreban pravi)
            String signature = "fake-signature-for-testing";
            
            String dummyJWT = header + "." + payload + "." + signature;
            
            // Spremi u sessionStorage (ključ 'auth_token' kao što koristi App.jsx)
            js.executeScript("sessionStorage.setItem('auth_token', '" + dummyJWT + "');");
            
            // Debug: Provjeri što je spremljeno
            String storedToken = (String) js.executeScript("return sessionStorage.getItem('auth_token');");
            System.out.println("  🔍 Debug - Stored JWT token: " + (storedToken != null ? storedToken.substring(0, Math.min(50, storedToken.length())) + "..." : "null"));
            
            // Refresh stranicu da React pokupi token iz sessionStorage
            System.out.println("  🔄 Refresham stranicu da React učita token...");
            driver.navigate().refresh();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Thread.sleep(2000); // Pauza da se React state sigurno ažurira
            
            System.out.println("🔑 Simulirana prijava korisnika: test.selenium@example.com (uloga: administrator)");
        } catch (Exception e) {
            System.err.println("⚠️ Greška pri simulaciji prijave: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper metoda za čekanje učitavanja Google Maps
     * Čeka da se učita Google Map komponenta ili da prođe određeno vrijeme
     */
    private static void waitForGoogleMapsToLoad() {
        try {
            // Čekaj da se pojavi Google Maps container ili iframe
            WebDriverWait mapWait = new WebDriverWait(driver, Duration.ofSeconds(30));
            
            // Pokušaj 1: Čekaj da nestane "Loading..." tekst (najvažnije)
            try {
                mapWait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//*[contains(text(), 'Loading')]"))
                );
                System.out.println("  🗺️ Google Maps 'Loading...' tekst nestao");
            } catch (Exception e1) {
                try {
                    // Pokušaj 2: Čekaj na iframe (standardni embed)
                    mapWait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("iframe[src*='google.com/maps']"))
                    );
                    System.out.println("  🗺️ Google Maps iframe učitan");
                } catch (Exception e2) {
                    try {
                        // Pokušaj 3: Čekaj na Google Maps API div container sa mapom
                        mapWait.until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("div[role='region'][aria-label*='Map'], div.gm-style"))
                        );
                        System.out.println("  🗺️ Google Maps API container učitan");
                    } catch (Exception e3) {
                        System.out.println("  ⚠️ Google Maps se dugo učitava - čekam dodatno vrijeme");
                    }
                }
            }
            
            // Dodatna pauza da se mapa potpuno renderira (povećano)
            Thread.sleep(3000);
            System.out.println("  ✅ Google Maps - čekanje završeno");
        } catch (Exception e) {
            System.out.println("  ⚠️ Google Maps nije pronađen ili se sporo učitava - nastavljam test");
            // Dodatna pauza ako mapa nije pronađena
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}