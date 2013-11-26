-- Take care to table ID (related to other loc files)
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (1, 'eng', 'title', '');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (2, 'eng', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (3, 'eng', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (4, 'eng', 'accessConstraints', '');

INSERT INTO CategoriesDes (iddes, langid, label) VALUES (1, 'eng', 'Default');

INSERT INTO GroupsDes (iddes, langid, label) VALUES (-1,'eng','Guest');
INSERT INTO GroupsDes (iddes, langid, label) VALUES (0,'eng','Intranet');
INSERT INTO GroupsDes (iddes, langid, label) VALUES (1,'eng','All');
INSERT INTO GroupsDes (iddes, langid, label) VALUES (2,'eng','Sample group');

INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (123,   	'eng',	'English');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (137,   	'eng',	'French');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (150,   	'eng',	'German');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (358,   	'eng',	'Rumantsch');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (124,   	'eng',	'Italien');

INSERT INTO OperationsDes  (iddes, langid, label) VALUES (0,'eng','Publish');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (1,'eng','Download');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (2,'eng','Editing');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (3,'eng','Notify');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (5,'eng','Interactive Map');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (6,'eng','Featured');


INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'eng','Unknown');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'eng','Draft');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'eng','Approved');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'eng','Retired');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'eng','Submitted');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'eng','Rejected');

