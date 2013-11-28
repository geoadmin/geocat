-- Take care to table ID (related to other loc files)
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (53, 'roh', 'title', '');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (54, 'roh', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (55, 'roh', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (56, 'roh', 'accessConstraints', '');

INSERT INTO CategoriesDes (iddes, langid, label)  VALUES (1, 'roh', 'Default');

INSERT INTO GroupsDes (iddes, langid, label)  VALUES (-1,'roh','Guest');
INSERT INTO GroupsDes (iddes, langid, label)  VALUES (0,'roh','Intranet');
INSERT INTO GroupsDes (iddes, langid, label)  VALUES (1,'roh','All');
INSERT INTO GroupsDes (iddes, langid, label)  VALUES (2,'roh','Sample group');


INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (123,   	'roh',	'English');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (137,   	'roh',	'French');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (150,   	'roh',	'German');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (358,   	'roh',	'Rumantsch');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (124,   	'roh',	'Italien');

INSERT INTO OperationsDes (iddes, langid, label)  VALUES (0,'roh','Publish');
INSERT INTO OperationsDes (iddes, langid, label)  VALUES (1,'roh','Download');
INSERT INTO OperationsDes (iddes, langid, label)  VALUES (2,'roh','Editing');
INSERT INTO OperationsDes (iddes, langid, label)  VALUES (3,'roh','Notify');
INSERT INTO OperationsDes (iddes, langid, label)  VALUES (5,'roh','Interactive Map');
INSERT INTO OperationsDes (iddes, langid, label)  VALUES (6,'roh','Featured');

INSERT INTO StatusValuesDes (iddes, langid, label) VALUES (0,'roh','Unknown');
INSERT INTO StatusValuesDes (iddes, langid, label) VALUES (1,'roh','Draft');
INSERT INTO StatusValuesDes (iddes, langid, label) VALUES (2,'roh','Approved');
INSERT INTO StatusValuesDes (iddes, langid, label) VALUES (3,'roh','Retired');
INSERT INTO StatusValuesDes (iddes, langid, label) VALUES (4,'roh','Submitted');
INSERT INTO StatusValuesDes (iddes, langid, label) VALUES (5,'roh','Rejected');