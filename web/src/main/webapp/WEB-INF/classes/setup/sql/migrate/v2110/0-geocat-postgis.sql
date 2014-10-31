-- Table: services
CREATE TABLE services
(
  id integer NOT NULL,
  class character varying(1024) NOT NULL,
  description character varying(1024),
  name character varying(255) NOT NULL,
  CONSTRAINT services_pkey PRIMARY KEY (id),
  CONSTRAINT services_unique_name UNIQUE (name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE services
  OWNER TO admin;

ALTER TABLE metadata ADD COLUMN extra character varying(255);

ALTER TABLE schematron ADD COLUMN displaypriority integer;
UPDATE schematron SET displaypriority=0;
ALTER TABLE schematron ALTER displaypriority SET NOT NULL;
