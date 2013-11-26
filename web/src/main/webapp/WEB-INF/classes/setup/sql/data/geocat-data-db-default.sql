-- DELETE FROM categoriesdes;
DELETE FROM categories;
INSERT INTO categories VALUES (1, 'default');

DELETE FROM isolanguagesdes;
DELETE FROM IsoLanguages;

-- DELETE FROM StatusValuesDes;

INSERT INTO Languages (id, name, isinspire, isdefault) VALUES  ('ita', 'Italian', 'y', 'n');
INSERT INTO isolanguages (id, code, shortcode) VALUES (124,	'ita',	'it');

INSERT INTO Languages (id, name, isinspire, isdefault) VALUES  ('eng', 'English', 'y', 'n');
INSERT INTO isolanguages (id, code, shortcode) VALUES (123,	'eng',	'en');

INSERT INTO Languages (id, name, isinspire, isdefault) VALUES  ('fre', 'French', 'y', 'n');
INSERT INTO isolanguages (id, code, shortcode) VALUES (137,	'fre',	'fr');

INSERT INTO Languages (id, name, isinspire, isdefault) VALUES  ('ger', 'German', 'y', 'n');
INSERT INTO isolanguages (id, code, shortcode) VALUES (150,	'ger',	'de');

INSERT INTO Languages (id, name, isinspire, isdefault) VALUES  ('roh', 'Rumantsch', 'y', 'n');
INSERT INTO isolanguages (id, code, shortcode) VALUES (358,	'roh',	'rm');
