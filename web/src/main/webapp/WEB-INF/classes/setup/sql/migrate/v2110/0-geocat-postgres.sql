
ALTER TABLE metadata ADD COLUMN extra character varying(255);

ALTER TABLE schematron ADD COLUMN displaypriority integer;
UPDATE schematron SET displaypriority=0;
ALTER TABLE schematron ALTER displaypriority SET NOT NULL;
ALTER TABLE schematron RENAME COLUMN file TO filename;
UPDATE schematroncriteria SET uitype='Group' where type = 'GROUP';

-- ALTER TABLE requests RENAME TO requests_v2;
-- ALTER TABLE params RENAME TO params_v2;

DROP TABLE spatialindex CASCADE;
DROP TABLE "spatialIndex";