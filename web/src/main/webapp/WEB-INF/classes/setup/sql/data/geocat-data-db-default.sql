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

INSERT INTO Groups (ID, DESCRIPTION, EMAIL, LOGO, NAME, REFERRER, WEBSITE) VALUES (42,'Test Group',NULL,NULL,'Test Group',0,NULL);
INSERT INTO GroupsDes (IDDES, LANGID, LABEL) VALUES (42,'eng','Test Group');
INSERT INTO GroupsDes (IDDES, LANGID, LABEL) VALUES (42,'fre','Test Group');
INSERT INTO GroupsDes (IDDES, LANGID, LABEL) VALUES (42,'ger','Test Group');
INSERT INTO GroupsDes (IDDES, LANGID, LABEL) VALUES (42,'ita','Test Group');
INSERT INTO GroupsDes (IDDES, LANGID, LABEL) VALUES (42,'roh','Test Group');

INSERT INTO public.schematron (id, filename, schemaname, displaypriority) VALUES (1, 'schematron-rules-inspire-strict.disabled.xsl', 'iso19139.che', 0);
INSERT INTO public.schematron (id, filename, schemaname, displaypriority) VALUES (2, 'schematron-rules-bgdi.required.xsl', 'iso19139.che', 1);
INSERT INTO public.schematron (id, filename, schemaname, displaypriority) VALUES (3, 'schematron-rules-geobasisdatensatz.required.xsl', 'iso19139.che', 2);

INSERT INTO public.schematroncriteriagroup (name, schematronid, requirement) VALUES ('Lichtenstein', 1, 'REQUIRED');
INSERT INTO public.schematroncriteriagroup (name, schematronid, requirement) VALUES ('DefaultInspireStrict', 1, 'REPORT_ONLY');
INSERT INTO public.schematroncriteriagroup (name, schematronid, requirement) VALUES ('BGDI', 2, 'REQUIRED');
INSERT INTO public.schematroncriteriagroup (name, schematronid, requirement) VALUES ('Geobasisdatensatz', 3, 'REQUIRED');

-- INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (1, 'GROUP', '', 'Lichtenstein', 1);
INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (1, 'XPATH', '*//gmd:keyword/gco:CharacterString/text() = ''BGDI''__OR__*//gmd:keyword/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString/text() = ''BGDI''', 'BGDI', 2);
INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (2, 'GROUP', '42', 'BGDI', 2);
INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (3, 'XPATH', '*//gmd:keyword/gco:CharacterString/text() = ''Geobasisdatensatz''__OR__*//gmd:keyword/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString/text() = ''Geobasisdatensatz''', 'Geobasisdatensatz', 3);
INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (4, 'GROUP', '42', 'Geobasisdatensatz', 3);
INSERT INTO public.schematroncriteria (id, type, value, group_name, group_schematronid) VALUES (5, 'ALWAYS_ACCEPT', '', 'DefaultInspireStrict', 1);
