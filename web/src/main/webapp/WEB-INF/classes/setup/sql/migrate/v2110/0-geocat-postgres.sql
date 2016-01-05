ALTER TABLE deletedobjects ALTER COLUMN deletiondate SET DATA TYPE character varying(30);

ALTER TABLE metadata ADD COLUMN extra character varying(255);

ALTER TABLE schematroncriteria ADD COLUMN uitype character varying(255);
ALTER TABLE schematroncriteria ADD COLUMN uivalue character varying(255);

ALTER TABLE schematron ADD COLUMN displaypriority integer;
UPDATE schematron SET displaypriority=0;
ALTER TABLE schematron ALTER displaypriority SET NOT NULL;
ALTER TABLE schematron RENAME COLUMN file TO filename;
UPDATE schematroncriteria SET uitype='Group' where type = 'GROUP';

ALTER TABLE requests RENAME TO requests_v2;
ALTER TABLE params RENAME TO params_v2;

DROP TABLE if exists spatialindex CASCADE;
DROP TABLE if exists "spatialIndex" CASCADE;

CREATE TABLE spatialindex
(
  gid serial NOT NULL,
  metadataId text,
  the_geom geometry,
  CONSTRAINT spatialindex_pkey PRIMARY KEY (gid),
  CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
  CONSTRAINT enforce_geotype_the_geom CHECK (geometrytype(the_geom) = 'MULTIPOLYGON'::text OR the_geom IS NULL),
  CONSTRAINT enforce_srid_the_geom CHECK (srid(the_geom) = 4326)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE spatialindex OWNER TO "www-data";
GRANT ALL ON TABLE spatialindex TO "www-data";

-- Index: "spatialIndex_the_geom_gist"

-- DROP INDEX "spatialIndex_the_geom_gist";

CREATE INDEX spatialindex_the_geom_gist
  ON spatialindex
  USING gist
  (the_geom);


INSERT INTO Operations (id, name) VALUES (2, 'editing');
INSERT INTO Operations (id, name) VALUES (5, 'dynamic');
INSERT INTO Operations (id, name) VALUES (6, 'featured');

INSERT INTO OperationsDes (iddes, label, langid) VALUES (2,'Editing','eng');
INSERT INTO OperationsDes (iddes, label, langid) VALUES (2,'Editer','fre');
INSERT INTO OperationsDes (iddes, label, langid) VALUES (2,'Editieren','ger');
INSERT INTO OperationsDes (iddes, label, langid) VALUES (2,'Modifica','ita');

INSERT INTO OperationsDes (iddes, label, langid) VALUES (5,'Interactive Map','eng');
INSERT INTO OperationsDes (iddes, label, langid) VALUES (5,'Carte interactive','fre');
INSERT INTO OperationsDes (iddes, label, langid) VALUES (5,'Interaktive Karte','ger');
INSERT INTO OperationsDes (iddes, label, langid) VALUES (5,'Mappa interattiva','ita');

INSERT INTO OperationsDes (iddes, label, langid) VALUES (6,'Featured','eng');
INSERT INTO OperationsDes (iddes, label, langid) VALUES (6,'Epingler','fre');
INSERT INTO OperationsDes (iddes, label, langid) VALUES (6,'Featured','ger');
INSERT INTO OperationsDes (iddes, label, langid) VALUES (6,'In rilievo','ita');


ALTER TABLE publish_tracking ADD COLUMN id integer;
CREATE SEQUENCE publish_record_id_seq START 1;
UPDATE publish_tracking SET id=nextval('publish_record_id_seq');
ALTER TABLE publish_tracking ALTER id SET NOT NULL;
ALTER TABLE publish_tracking RENAME published TO jpaworkaround_published;
ALTER TABLE publish_tracking RENAME validated TO jpaworkaround_validated;

truncate statusvaluesdes;

