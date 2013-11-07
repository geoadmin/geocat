INSERT INTO Languages (id, name, isinspire, isdefault) VALUES ('fre','français', 'y', 'n');

-- Take care to table ID (related to other loc files)
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (21, 'fre', 'title', '');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (22, 'fre', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (23, 'fre', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo (idfield, langid, field, label) VALUES (24, 'fre', 'accessConstraints', '');

INSERT INTO CategoriesDes (iddes, langid, label) VALUES (2,'fre','Jeux de données');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (1,'fre','Cartes & graphiques');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (7,'fre','Photographies');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (10,'fre','Autres ressources');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (5,'fre','Etude de cas, meilleures pratiques');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (8,'fre','Vidéo/Audio');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (9,'fre','Répertoires');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (4,'fre','Applications');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (3,'fre','Ressources interactives');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (6,'fre','Conférences');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (11,'fre','Serveurs Z3950');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (12,'fre','Annuaires');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (13,'fre','Echantillons physiques');

INSERT INTO CategoriesDes VALUES (1, 'fre', 'Défaut');

INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (123,   	'fre',	'Anglais');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (137,   	'fre',	'Français');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (150,   	'fre',	'Allemand');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (358,   	'fre',	'Rumantsch');
INSERT INTO IsoLanguagesDes (iddes, langid, label) VALUES (124,   	'fre',	'Italien');

INSERT INTO OperationsDes  (iddes, langid, label) VALUES (0,'fre','Publier');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (1,'fre','Télécharger');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (2,'fre','Editer');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (3,'fre','Notifier');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (5,'fre','Carte interactive');
INSERT INTO OperationsDes  (iddes, langid, label) VALUES (6,'fre','Epingler');

INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'fre','Inconnu');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'fre','Brouillon');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'fre','Validé');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'fre','Retiré');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'fre','A valider');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'fre','Rejeté');

