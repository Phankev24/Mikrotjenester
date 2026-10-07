-- Gjør UUID-kolonnen til primærnøkkel og behold eksisterende user_id som seq_id

BEGIN;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1) Gi nytt navn på user_id til seq_id (beholder sekvensen og verdiene)
ALTER TABLE vendor RENAME COLUMN user_id TO seq_id;

-- 2) Sørg for at seq_id har en sequence default (hvis original var bigserial, finnes allerede)
-- Hvis seq_id mangler DEFAULT, opprett en sequence og sett default:
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM pg_attrdef a
                              JOIN pg_class c ON a.adrelid = c.oid
                              JOIN pg_attribute att ON att.attrelid = c.oid AND att.attnum = a.adnum
            WHERE c.relname = 'vendor' AND att.attname = 'seq_id'
        ) THEN
            -- Opprett en ny sekvens og sett som default for seq_id
            CREATE SEQUENCE IF NOT EXISTS vendor_seq_id_seq;
            ALTER TABLE vendor ALTER COLUMN seq_id SET DEFAULT nextval('vendor_seq_id_seq');
            -- Fyll eksisterende NULLer (sannsynligvis ingen) med nextval
            UPDATE vendor SET seq_id = nextval('vendor_seq_id_seq') WHERE seq_id IS NULL;
        END IF;
    END$$;

-- 3) Fjern gammel primary key constraint (hvis den fortsatt peker på seq_id)
ALTER TABLE vendor DROP CONSTRAINT IF EXISTS vendor_pkey;

ALTER TABLE vendor ALTER COLUMN id SET NOT NULL;

-- 5) Sett id som ny primærnøkkel
ALTER TABLE vendor ADD PRIMARY KEY (id);

-- 6) Legg til unik indeks på seq_id for raskt søk og for å sikre unikhet
CREATE UNIQUE INDEX IF NOT EXISTS ux_vendor_seq_id ON vendor(seq_id);

COMMIT;
