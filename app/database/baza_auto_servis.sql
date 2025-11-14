--CREATE TYPE tip_uloge AS ENUM ('korisnik', 'serviser', 'administrator');
--CREATE TYPE status_prijave AS ENUM ('zaprimljeno', 'u obradi', 'završeno', 'odgođeno');
--CREATE TYPE tip_obrasca AS ENUM ('predaja', 'preuzimanje');
--CREATE TYPE format_izvjestaja AS ENUM ('pdf', 'xml', 'xlsx');

CREATE TABLE osoba (
    id_osoba SERIAL PRIMARY KEY,
    ime VARCHAR(100) NOT NULL,
    prezime VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL CHECK (
	  email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
	),
    telefon VARCHAR(20) CHECK (telefon ~ '^\+?[0-9\s\-]+$'),
    --uloga tip_uloge NOT NULL DEFAULT 'korisnik',
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
    godina_proizvodnje INT CHECK (godina_proizvodnje BETWEEN 1950 AND EXTRACT(YEAR FROM CURRENT_DATE))

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
    --trajanje INTERVAL NOT NULL,
    --end_time TIMESTAMP GENERATED ALWAYS AS (datum_vrijeme + trajanje) STORED,
    zauzet BOOLEAN DEFAULT FALSE
);

CREATE TABLE prijava_servisa (
    id_prijava SERIAL PRIMARY KEY,
    id_vozilo INT REFERENCES vozilo(id_vozilo) ON DELETE CASCADE,
    id_serviser INT REFERENCES serviser(id_serviser) ON DELETE SET NULL,
    id_termin INT REFERENCES termin(id_termin) ON DELETE SET NULL,
    --status status_prijave DEFAULT 'zaprimljeno'::status_prijave NOT NULL,
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
    datum_do DATE NOT NULL,
    CHECK (datum_do >= datum_od)
);

CREATE TABLE napomena_servisera (
    id_napomena SERIAL PRIMARY KEY,
    id_prijava INT REFERENCES prijava_servisa(id_prijava) ON DELETE CASCADE,
    datum TIMESTAMP NOT NULL,
    opis TEXT NOT NULL -- da je null nebi imalo smisla
);

CREATE TABLE obrazac (
    id_obrazac SERIAL PRIMARY KEY,
    id_prijava INT REFERENCES prijava_servisa(id_prijava) ON DELETE CASCADE,
    --tip tip_obrasca NOT NULL,
    tip VARCHAR(50) CHECK (tip IN ('predaja', 'preuzimanje')),
    putanja_pdf TEXT NOT NULL,
    datum_generiranja TIMESTAMP NOT NULL
);

CREATE TABLE izvjestaj (
    id_izvjestaj SERIAL PRIMARY KEY,
    --format format_izvjestaja NOT NULL,
    format VARCHAR(50) CHECK (format IN ('pdf', 'xml', 'xlsx')),
    datum_generiranja TIMESTAMP NOT NULL,
    putanja_dat TEXT NOT NULL
);

-- indeksi za ubrzanje pretrage
CREATE INDEX idx_osoba_email ON osoba(email);
CREATE INDEX idx_vozilo_registracija ON vozilo(registracija);
CREATE INDEX idx_prijava_status ON prijava_servisa(status);
CREATE INDEX idx_prijava_serviser ON prijava_servisa(id_serviser);
CREATE INDEX idx_marka_naziv ON marka(naziv);
CREATE INDEX idx_serviser_voditelj ON serviser (id_servis, je_li_voditelj);

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