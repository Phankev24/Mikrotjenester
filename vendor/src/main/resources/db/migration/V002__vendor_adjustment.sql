ALTER TABLE vendor ALTER COLUMN event_id DROP NOT NULL;
ALTER TABLE vendor ADD COLUMN external_id UUID;
UPDATE vendor SET external_id = gen_random_uuid() WHERE external_id IS NULL;
ALTER TABLE vendor ALTER COLUMN external_id SET NOT NULL;
ALTER TABLE vendor ADD CONSTRAINT uk_vendor_external_id UNIQUE (external_id);
