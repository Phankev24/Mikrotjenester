-- V002__make_id_bigserial_and_userid_uuid.sql
BEGIN;

ALTER TABLE vendor
    ADD COLUMN IF NOT EXISTS user_id_uuid uuid;

UPDATE vendor
SET user_id_uuid = id
WHERE user_id_uuid IS NULL AND id IS NOT NULL;

-- 2) Fjern eksisterende primary key constraint (som peker på user_id eller annet)
ALTER TABLE vendor DROP CONSTRAINT IF EXISTS vendor_pkey;

-- 3) Legg til ny kolonne new_id som bigserial (DB genererer sekvens)
ALTER TABLE vendor ADD COLUMN IF NOT EXISTS new_id bigserial;

UPDATE vendor
SET new_id = user_id
WHERE new_id IS NULL AND user_id IS NOT NULL;

-- 5) Hvis noen rader fortsatt har NULL new_id, la sekvens fylle dem:
--    (nextval vil settes automatisk ved INSERT; for eksisterende NULL, sett nextval)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM vendor WHERE new_id IS NULL) THEN
UPDATE vendor SET new_id = nextval(pg_get_serial_sequence('vendor','new_id')) WHERE new_id IS NULL;
END IF;
END$$;

-- 6) Sett new_id som primærnøkkel
ALTER TABLE vendor ADD PRIMARY KEY (new_id);

-- 7) Bytt navn på kolonnene: new_id -> id, user_id_uuid -> user_id
ALTER TABLE vendor RENAME COLUMN new_id TO id;
ALTER TABLE vendor RENAME COLUMN user_id_uuid TO user_id;

-- 8) Sørg for at user_id er uuid NOT NULL (hvis du ønsker det)
ALTER TABLE vendor ALTER COLUMN user_id SET NOT NULL;

-- 9) Legg til unik indeks på user_id hvis ønskelig
CREATE UNIQUE INDEX IF NOT EXISTS ux_vendor_user_id ON vendor(user_id);

COMMIT;
