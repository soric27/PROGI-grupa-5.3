CREATE TABLE osoba (
    id_osoba SERIAL PRIMARY KEY,
    ime VARCHAR(100) NOT NULL,
    prezime VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL CHECK (
	  email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
	),
    telefon VARCHAR(20) CHECK (telefon ~ '^\+?[0-9\s]+$'),
    uloga VARCHAR(50) CHECK (uloga IN ('korisnik', 'serviser', 'administrator')),
    oauth_id VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE marka (
    id_marka SERIAL PRIMARY KEY,
    naziv VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE model (
    id_model SERIAL PRIMARY KEY,
    id_marka INT REFERENCES marka(id_marka) ON DELETE CASCADE,
    naziv VARCHAR(50) NOT NULL,
    UNIQUE (id_marka, naziv)
);

CREATE TABLE vozilo (
    id_vozilo SERIAL PRIMARY KEY,
    id_osoba INT REFERENCES osoba(id_osoba) ON DELETE CASCADE,--vlasnik
    id_model INT REFERENCES model(id_model) ON DELETE RESTRICT,
    registracija VARCHAR(20) UNIQUE NOT NULL,
    godina_proizvodnje INT CHECK (godina_proizvodnje <= EXTRACT(YEAR FROM CURRENT_DATE))
);

CREATE TABLE servis (
    id_servis SERIAL PRIMARY KEY,
    ime_servisa VARCHAR(100) NOT NULL,
    lokacija TEXT -- kasnije s Google Mapsom prepraviti
);

CREATE TABLE serviser (
    id_serviser SERIAL PRIMARY KEY,
    id_osoba INT REFERENCES osoba(id_osoba) ON DELETE CASCADE,
    id_servis INT REFERENCES servis(id_servis) ON DELETE SET NULL,
    je_li_voditelj BOOLEAN DEFAULT FALSE  -- za statistiku ce trebat
);


CREATE TABLE termin (
    id_termin SERIAL PRIMARY KEY,
    datum_vrijeme TIMESTAMP NOT NULL,
    zauzet BOOLEAN DEFAULT FALSE
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
    datum_generiranja TIMESTAMP NOT NULL
);

CREATE TABLE izvjestaj (
    id_izvjestaj SERIAL PRIMARY KEY,
    format VARCHAR(20) CHECK (format IN ('pdf', 'xml', 'xlsx')),
    datum_generiranja TIMESTAMP NOT NULL,
    putanja_dat TEXT NOT NULL
);

INSERT INTO marka (naziv) VALUES
('Audi'),
('BMW'),
('Volkswagen'),
('Mercedes-Benz'),
('Opel'),
('Škoda'),
('Toyota'),
('Ford'),
('Peugeot'),
('Renault'),
('Hyundai'),
('Kia'),
('Fiat');

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


-- ===== Per-serviser termini seed (idempotent, Postgres-safe) =====
-- Add column if missing and FK if possible, then insert servisere and 7 days x 9..16 terms

-- If you are currently in an aborted transaction in psql, run: ROLLBACK; or open a new connection before running this script.

ALTER TABLE termin ADD COLUMN IF NOT EXISTS id_serviser bigint;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='serviser') THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints tc
      JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
      WHERE tc.table_name = 'termin' AND tc.constraint_type = 'FOREIGN KEY' AND kcu.column_name = 'id_serviser'
    ) THEN
      BEGIN
        ALTER TABLE termin ADD CONSTRAINT fk_termin_serviser FOREIGN KEY (id_serviser) REFERENCES serviser(id_serviser);
      EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'Could not add fk_termin_serviser: %', SQLERRM;
      END;
    END IF;
  ELSE
    RAISE NOTICE 'Table serviser not found, skipping FK creation.';
  END IF;
END$$;

-- Create sample servisers (idempotent: uses existing osoba by email)
DO $$
BEGIN
  INSERT INTO osoba (ime, prezime, email, uloga, oauth_id)
    SELECT 'Ivan','Ivić','ivan.serviser@example.com','serviser','srv-ivan'
    WHERE NOT EXISTS (SELECT 1 FROM osoba WHERE email='ivan.serviser@example.com');
  INSERT INTO osoba (ime, prezime, email, uloga, oauth_id)
    SELECT 'Marko','Marković','marko.serviser@example.com','serviser','srv-marko'
    WHERE NOT EXISTS (SELECT 1 FROM osoba WHERE email='marko.serviser@example.com');
  INSERT INTO osoba (ime, prezime, email, uloga, oauth_id)
    SELECT 'Ana','Anić','ana.serviser@example.com','serviser','srv-ana'
    WHERE NOT EXISTS (SELECT 1 FROM osoba WHERE email='ana.serviser@example.com');
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Skipping osoba inserts (maybe already present): %', SQLERRM;
END$$;

DO $$
BEGIN
  INSERT INTO serviser (id_osoba, je_li_voditelj)
    SELECT o.id_osoba, false FROM osoba o WHERE o.email = 'ivan.serviser@example.com' AND NOT EXISTS (SELECT 1 FROM serviser s JOIN osoba oo ON s.id_osoba = oo.id_osoba WHERE oo.email = 'ivan.serviser@example.com');
  INSERT INTO serviser (id_osoba, je_li_voditelj)
    SELECT o.id_osoba, false FROM osoba o WHERE o.email = 'marko.serviser@example.com' AND NOT EXISTS (SELECT 1 FROM serviser s JOIN osoba oo ON s.id_osoba = oo.id_osoba WHERE oo.email = 'marko.serviser@example.com');
  INSERT INTO serviser (id_osoba, je_li_voditelj)
    SELECT o.id_osoba, false FROM osoba o WHERE o.email = 'ana.serviser@example.com' AND NOT EXISTS (SELECT 1 FROM serviser s JOIN osoba oo ON s.id_osoba = oo.id_osoba WHERE oo.email = 'ana.serviser@example.com');
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Skipping serviser inserts (maybe already present): %', SQLERRM;
END$$;

-- Insert terms for next 7 days (9:00 - 16:00) for each serviser, avoid duplicates
DO $$
DECLARE
  d date := current_date;
  s record;
  h int;
BEGIN
  FOR s IN SELECT id_serviser FROM serviser LOOP
    FOR h IN 9..16 LOOP
      FOR i IN 0..6 LOOP
        PERFORM 1 FROM termin t WHERE t.datum_vrijeme = ((d + i) + (h || ':00')::time) AND t.id_serviser = s.id_serviser;
        IF NOT FOUND THEN
          INSERT INTO termin (datum_vrijeme, zauzet, id_serviser)
            VALUES ((d + i) + (h || ':00')::time, false, s.id_serviser);
        END IF;
      END LOOP;
    END LOOP;
  END LOOP;
END$$;

-- Quick checks (uncomment to run manually):
-- SELECT COUNT(*) FROM termin WHERE id_serviser IS NULL;
-- SELECT id_serviser, COUNT(*) AS cnt FROM termin GROUP BY id_serviser ORDER BY id_serviser;
-- =====================================================