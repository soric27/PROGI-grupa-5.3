CREATE TABLE osoba (
    id_osoba SERIAL PRIMARY KEY,
    ime VARCHAR(100) NOT NULL,
    prezime VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefon VARCHAR(20),
    uloga VARCHAR(50) CHECK (uloga IN ('korisnik', 'serviser', 'administrator')),
    oauth_id VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE vozilo (
    id_vozilo SERIAL PRIMARY KEY,
    id_osoba INT REFERENCES osoba(id_osoba) ON DELETE CASCADE,--vlasnik
    marka VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
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
    marka VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
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
