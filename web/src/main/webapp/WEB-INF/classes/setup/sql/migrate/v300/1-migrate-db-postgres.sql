-- Creates New tables required for this version


ALTER TABLE schematron ADD COLUMN displaypriority integer NOT NULL;
UPDATE schematron SET displaypriority=0;

