
ALTER TABLE metadata ADD COLUMN extra character varying(255);

ALTER TABLE schematron ADD COLUMN displaypriority integer;
ALTER TABLE schematroncriteria ADD COLUMN uitype character varying(255);
ALTER TABLE schematroncriteria ADD COLUMN uivalue character varying(255);

UPDATE schematron SET displaypriority=0;
ALTER TABLE schematron ALTER displaypriority SET NOT NULL;
ALTER TABLE schematron RENAME COLUMN file TO filename;
UPDATE schematroncriteria SET uitype='Group' where type = 'GROUP';

ALTER TABLE requests RENAME TO requests_v2;
ALTER TABLE params RENAME TO params_v2;

DROP TABLE spatialindex CASCADE;
DROP TABLE "spatialIndex";

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
