-- Seed: 3 servisera, svaki s 8 termina (9:00 - 16:00) za narednih 7 dana
-- Prilagodi datume ako želiš drugačiji raspon

-- Ensure schema supports per-serviser terms (Postgres-safe)
ALTER TABLE termin ADD COLUMN IF NOT EXISTS id_serviser bigint;

-- Add FK only if serviser table exists, and guard against errors so the script continues
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

-- The following inserts are wrapped individually so one failing block won't abort the whole script


-- Kreiraj osobe (serviseri)
DO $$
BEGIN
  INSERT INTO osoba (ime, prezime, email, uloga, oauth_id) VALUES
  ('Ivan','Ivić','ivan.serviser@example.com','serviser','srv-ivan'),
  ('Marko','Marković','marko.serviser@example.com','serviser','srv-marko'),
  ('Ana','Anić','ana.serviser@example.com','serviser','srv-ana');
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Skipping osoba inserts (maybe already present): %', SQLERRM;
END$$;

-- Pretpostavljamo da su id-evi 1,2,3 (provjeri sa SELECT id_osoba FROM osoba)
-- Ako nisu, prilagodi id-eve u nastavku ili upotrijebi RETURNING

-- Kreiraj servisere (referencira tablicu osoba)
DO $$
BEGIN
  INSERT INTO serviser (id_osoba, je_li_voditelj) VALUES
  ((SELECT id_osoba FROM osoba WHERE email='ivan.serviser@example.com'), false),
  ((SELECT id_osoba FROM osoba WHERE email='marko.serviser@example.com'), false),
  ((SELECT id_osoba FROM osoba WHERE email='ana.serviser@example.com'), false);
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Skipping serviser inserts (maybe already present): %', SQLERRM;
END$$;

-- Sada umetni termine: za svaku od sljedećih 7 dana, za svaki serviser, po jedan termin po satu 9..16
-- Datum koristi svoju lokalnu zonu - primjer koristi PostgreSQL timestamp literal
DO $$
DECLARE
 d date := current_date;
 s record;
 h int;
 i int := 0;
BEGIN
  FOR i IN 0..6 LOOP
    FOR s IN SELECT id_serviser FROM serviser LOOP
      FOR h IN 9..16 LOOP
        INSERT INTO termin (datum_vrijeme, zauzet, id_serviser) VALUES ( (d + i) + (h || ':00')::time, false, s.id_serviser );
      END LOOP;
    END LOOP;
  END LOOP;
END$$;

COMMIT;

-- Napomene:
-- 1) Ako koristiš MySQL, prilagodi DO/LOOP u uobičajeni set INSERT iz skripte.
-- 2) Svaki termin je sada vezan za konkretnog servisera (polje id_serviser u tablici termin).
-- 3) Kada korisnik pošalje prijavu, backend će atomskog označiti termin kao zauzet (ako je još slobodan).
-- 4) Ako želiš samo jedinstvene termine koje viđaju svi serviseri, možeš umjesto id_serviser polja kreirati globalne termine.
