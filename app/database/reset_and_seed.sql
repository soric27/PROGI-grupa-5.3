/*
  reset_and_seed.sql
  ------------------
  Idempotent script to drop and recreate the entire schema and insert seed data
  (servisers, persons, brands/models, terms). Run in pgAdmin Query Tool or psql.

  WARNING: This will remove existing data in the affected tables. Run only if
  you are OK with wiping current DB state or on a fresh DB.
*/

-- Run in a fresh connection (some DDL forces commits) and wrap manual steps in a transaction if desired.
SET client_encoding = 'UTF8';

-- DROP all application tables (reverse dependencies)
DROP TABLE IF EXISTS prijava_kvar CASCADE;
DROP TABLE IF EXISTS kvar CASCADE;
DROP TABLE IF EXISTS rezervacija_zamjene CASCADE;
DROP TABLE IF EXISTS obrazac CASCADE;
DROP TABLE IF EXISTS napomena_servisera CASCADE;
DROP TABLE IF EXISTS prijava_servisa CASCADE;
DROP TABLE IF EXISTS termin CASCADE;
DROP TABLE IF EXISTS serviser CASCADE;
DROP TABLE IF EXISTS servis CASCADE;
DROP TABLE IF EXISTS vozilo CASCADE;
DROP TABLE IF EXISTS zamjena_vozilo CASCADE;
DROP TABLE IF EXISTS model CASCADE;
DROP TABLE IF EXISTS marka CASCADE;
DROP TABLE IF EXISTS osoba CASCADE;
DROP TABLE IF EXISTS izvjestaj CASCADE;

-- === CREATE SCHEMA ===

CREATE TABLE osoba (
    id_osoba SERIAL PRIMARY KEY,
    ime VARCHAR(100) NOT NULL,
    prezime VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL CHECK (
      email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
    ),
    telefon VARCHAR(20) CHECK (telefon ~ '^\+?[0-9\s]+$'),
    uloga VARCHAR(50) NOT NULL DEFAULT 'korisnik' CHECK (uloga IN ('korisnik', 'serviser', 'administrator')),
    oauth_id VARCHAR(255) UNIQUE
);

CREATE TABLE marka (
    id_marka SERIAL PRIMARY KEY,
    naziv VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE model (
    id_model SERIAL PRIMARY KEY,
    id_marka INT NOT NULL REFERENCES marka(id_marka) ON DELETE CASCADE,
    naziv VARCHAR(50) NOT NULL,
    UNIQUE (id_marka, naziv)
);

CREATE TABLE servis (
    id_servis SERIAL PRIMARY KEY,
    ime_servisa VARCHAR(100) NOT NULL,
    lokacija TEXT
);

CREATE TABLE serviser (
    id_serviser SERIAL PRIMARY KEY,
    id_osoba INT NOT NULL UNIQUE REFERENCES osoba(id_osoba) ON DELETE CASCADE,
    id_servis INT REFERENCES servis(id_servis) ON DELETE SET NULL,
    je_li_voditelj BOOLEAN DEFAULT FALSE
);

CREATE TABLE vozilo (
    id_vozilo SERIAL PRIMARY KEY,
    id_osoba INT REFERENCES osoba(id_osoba) ON DELETE CASCADE,
    id_model INT REFERENCES model(id_model) ON DELETE RESTRICT,
    registracija VARCHAR(20) UNIQUE NOT NULL,
    godina_proizvodnje INT CHECK (godina_proizvodnje <= EXTRACT(YEAR FROM CURRENT_DATE))
);

CREATE TABLE termin (
    id_termin SERIAL PRIMARY KEY,
    datum_vrijeme TIMESTAMP NOT NULL,
    zauzet BOOLEAN DEFAULT FALSE,
    id_serviser INT REFERENCES serviser(id_serviser) ON DELETE SET NULL
);

CREATE TABLE prijava_servisa (
    id_prijava SERIAL PRIMARY KEY,
    id_vozilo INT REFERENCES vozilo(id_vozilo) ON DELETE CASCADE,
    id_serviser INT REFERENCES serviser(id_serviser) ON DELETE SET NULL,
    id_termin INT REFERENCES termin(id_termin) ON DELETE SET NULL,
    status VARCHAR(50) CHECK (status IN ('zaprimljeno', 'u obradi', 'završeno', 'odgođeno')),
    datum_prijave TIMESTAMP NOT NULL,
    datum_predaje TIMESTAMP,
    datum_preuzimanja TIMESTAMP,
    napomena_vlasnika TEXT
);

CREATE TABLE zamjena_vozilo (
    id_zamjena SERIAL PRIMARY KEY,
    id_model INT REFERENCES model(id_model) ON DELETE RESTRICT,
    registracija VARCHAR(20) UNIQUE NOT NULL,
    dostupno BOOLEAN DEFAULT TRUE
);

CREATE TABLE kvar (
    id_kvar SERIAL PRIMARY KEY,
    naziv VARCHAR(100) NOT NULL UNIQUE,
    opis TEXT
);

CREATE TABLE prijava_kvar (
    id_prijava INT REFERENCES prijava_servisa(id_prijava) ON DELETE CASCADE,
    id_kvar INT REFERENCES kvar(id_kvar) ON DELETE CASCADE,
    PRIMARY KEY (id_prijava, id_kvar)
);

CREATE TABLE rezervacija_zamjene (
    id_rezervacija SERIAL PRIMARY KEY,
    id_prijava INT REFERENCES prijava_servisa(id_prijava) ON DELETE CASCADE,
    id_zamjena INT REFERENCES zamjena_vozilo(id_zamjena) ON DELETE CASCADE,
    datum_od DATE NOT NULL,
    datum_do DATE NOT NULL
);

CREATE TABLE napomena_servisera (
    id_napomena SERIAL PRIMARY KEY,
    id_prijava INT REFERENCES prijava_servisa(id_prijava) ON DELETE CASCADE,
    datum TIMESTAMP NOT NULL,
    opis TEXT
);

CREATE TABLE obrazac (
    id_obrazac SERIAL PRIMARY KEY,
    id_prijava INT REFERENCES prijava_servisa(id_prijava) ON DELETE CASCADE,
    tip VARCHAR(20) CHECK (tip IN ('predaja', 'preuzimanje')),
    putanja_pdf TEXT NOT NULL,
    pdf_data BYTEA,
    datum_generiranja TIMESTAMP NOT NULL
);

CREATE TABLE izvjestaj (
    id_izvjestaj SERIAL PRIMARY KEY,
    format VARCHAR(20) CHECK (format IN ('pdf', 'xml', 'xlsx')),
    datum_generiranja TIMESTAMP NOT NULL,
    putanja_dat TEXT NOT NULL
);

-- Extra table for service contact/info (application expects this table via JPA entity ServisInfo)
CREATE TABLE servis_info (
    id SERIAL PRIMARY KEY,
    contact_email VARCHAR(100),
    contact_phone VARCHAR(50),
    about_text TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8)
);

-- Seed a default servis_info row (idempotent)
INSERT INTO servis_info (contact_email, contact_phone, about_text, latitude, longitude)
SELECT 'info@autoservis.hr', '+38512345678', 'Auto Servis MK2 - najbolji servisi u gradu.', 45.815399, 15.966568
WHERE NOT EXISTS (SELECT 1 FROM servis_info);

-- === Seed data ===

-- Brands
INSERT INTO marka (naziv) VALUES
('Audi'),('BMW'),('Volkswagen'),('Mercedes-Benz'),('Opel'),('Škoda'),('Toyota'),('Ford'),('Peugeot'),('Renault'),('Hyundai'),('Kia'),('Fiat')
ON CONFLICT (naziv) DO NOTHING;

INSERT INTO model (id_marka, naziv) VALUES
-- Audi
(1, 'A1'), (1, 'A3'), (1, 'A4'), (1, 'A6'), (1, 'Q3'), (1, 'Q5'), (1, 'Q7'),

-- BMW
(2, '1 Series'), (2, '3 Series'), (2, '5 Series'), (2, 'X1'), (2, 'X3'), (2, 'X5'), (2, 'i3'),

-- Volkswagen
(3, 'Polo'), (3, 'Golf'), (3, 'Passat'), (3, 'Tiguan'), (3, 'Arteon'), (3, 'Touareg'),

-- Mercedes-Benz
(4, 'A-Class'), (4, 'B-Class'), (4, 'C-Class'), (4, 'E-Class'), (4, 'S-Class'), (4, 'GLA'), (4, 'GLC'),

-- Opel
(5, 'Corsa'), (5, 'Astra'), (5, 'Insignia'), (5, 'Mokka'), (5, 'Zafira'),

-- Škoda
(6, 'Fabia'), (6, 'Octavia'), (6, 'Superb'), (6, 'Karoq'), (6, 'Kodiaq'),

-- Toyota
(7, 'Yaris'), (7, 'Corolla'), (7, 'Camry'), (7, 'RAV4'), (7, 'C-HR'), (7, 'Hilux'),

-- Ford
(8, 'Fiesta'), (8, 'Focus'), (8, 'Mondeo'), (8, 'Kuga'), (8, 'Puma'),

-- Peugeot
(9, '208'), (9, '308'), (9, '3008'), (9, '5008'), (9, '508'),

-- Renault
(10, 'Clio'), (10, 'Megane'), (10, 'Captur'), (10, 'Kadjar'), (10, 'Talisman'),

-- Hyundai
(11, 'i10'), (11, 'i20'), (11, 'i30'), (11, 'Tucson'), (11, 'Santa Fe'),

-- Kia
(12, 'Picanto'), (12, 'Rio'), (12, 'Ceed'), (12, 'Sportage'), (12, 'Sorento'),

-- Fiat
(13, 'Panda'), (13, '500'), (13, 'Tipo'), (13, 'Punto'), (13, 'Doblo');


-- Example servis (optional)
INSERT INTO servis (ime_servisa, lokacija) VALUES
('AutoServis Centar','Adresa 1')
ON CONFLICT DO NOTHING;

-- Sample replacement vehicles (idempotent)
-- Use existing models by name to avoid hard-coded ids
INSERT INTO zamjena_vozilo (id_model, registracija, dostupno)
SELECT id_model, '123abc457', true FROM model WHERE naziv = 'A4' LIMIT 1
ON CONFLICT (registracija) DO NOTHING;

INSERT INTO zamjena_vozilo (id_model, registracija, dostupno)
SELECT id_model, 'st1234bd', true FROM model WHERE naziv = 'Corsa' LIMIT 1
ON CONFLICT (registracija) DO NOTHING;

-- Sample people (idempotent)
INSERT INTO osoba (ime, prezime, email, uloga, oauth_id)
  SELECT 'Ivan','Ivić','ivan.serviser@example.com','serviser','srv-ivan'
  WHERE NOT EXISTS (SELECT 1 FROM osoba WHERE email='ivan.serviser@example.com');

INSERT INTO osoba (ime, prezime, email, uloga, oauth_id)
  SELECT 'Marko','Marković','marko.serviser@example.com','serviser','srv-marko'
  WHERE NOT EXISTS (SELECT 1 FROM osoba WHERE email='marko.serviser@example.com');

INSERT INTO osoba (ime, prezime, email, uloga, oauth_id)
  SELECT 'Ana','Anić','ana.serviser@example.com','serviser','srv-ana'
  WHERE NOT EXISTS (SELECT 1 FROM osoba WHERE email='ana.serviser@example.com');

-- Ensure servisers exist for those persons
INSERT INTO serviser (id_osoba, je_li_voditelj)
  SELECT o.id_osoba, false FROM osoba o WHERE o.email = 'ivan.serviser@example.com' AND NOT EXISTS (SELECT 1 FROM serviser s WHERE s.id_osoba = o.id_osoba);

INSERT INTO serviser (id_osoba, je_li_voditelj)
  SELECT o.id_osoba, false FROM osoba o WHERE o.email = 'marko.serviser@example.com' AND NOT EXISTS (SELECT 1 FROM serviser s WHERE s.id_osoba = o.id_osoba);

INSERT INTO serviser (id_osoba, je_li_voditelj)
  SELECT o.id_osoba, false FROM osoba o WHERE o.email = 'ana.serviser@example.com' AND NOT EXISTS (SELECT 1 FROM serviser s WHERE s.id_osoba = o.id_osoba);

-- Seed common defects
INSERT INTO kvar (naziv, opis) VALUES
('Pokvarena svjetla', 'Glavna ili pozadinska svjetla nisu u funkciji'),
('Ne radi kočnica', 'Kočnični sustav nije u funkciji ili je neispravan'),
('Razbiven prozor', 'Staklo prozora je oštećeno ili razbijeno'),
('Problem s motorom', 'Motor ne radi kako treba ili ima čudne zvukove'),
('Istrošene gume', 'Gume su istrošene ili imaju pukotine'),
('Neispravan alarm', 'Alarm je neispravan ili ne funkcionira'),
('Odljev vode', 'Voda procuri unutar vozila'),
('Neispravan alternator', 'Alternator ne puni bateriju kako treba')
ON CONFLICT (naziv) DO NOTHING;
DO $$
DECLARE
  d date := current_date;
  s record;
  h int;
  i int;
  m int;
BEGIN
  FOR s IN SELECT id_serviser FROM serviser LOOP
    FOR h IN 9..16 LOOP
      FOR m IN 0..1 LOOP
        FOR i IN 0..6 LOOP
          -- avoid duplicates
          IF NOT EXISTS (
            SELECT 1 FROM termin t
            WHERE t.datum_vrijeme = ((d + i) + make_time(h, m * 30, 0))
              AND t.id_serviser = s.id_serviser
          ) THEN
            INSERT INTO termin (datum_vrijeme, zauzet, id_serviser)
              VALUES ((d + i) + make_time(h, m * 30, 0), false, s.id_serviser);
          END IF;
        END LOOP;
      END LOOP;
    END LOOP;
  END LOOP;
END$$;

-- Quick checks (user-friendly output)
-- SELECT COUNT(*) AS servisers_count FROM serviser;
-- SELECT id_serviser, (SELECT ime||' '||prezime FROM osoba o WHERE o.id_osoba = s.id_osoba) AS imePrezime FROM serviser s ORDER BY id_serviser;
-- SELECT id_serviser, COUNT(*) AS cnt FROM termin GROUP BY id_serviser ORDER BY id_serviser;

/* End of script */
