-- geocat.ch / Starting point
-- SELECT * FROM settings WHERE name LIKE '%version%'
-- = 3.6.0

-- ## 3.7.0
-- Copy the current UI setting
INSERT INTO Settings_ui (id, configuration) (SELECT 'srv', value FROM Settings WHERE name = 'ui/config');
DELETE FROM Settings WHERE name = 'ui/config';

-- ALTER TABLE Sources DROP COLUMN islocal;

UPDATE Metadata SET data = replace(data, 'http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml', 'http://standards.iso.org/iso/19139/resources/gmxCodelists.xml') WHERE data LIKE '%http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml%' AND schemaId LIKE 'iso19139%';


-- Update GML namespace for moving from ISO19139:2005 to ISO19139:2007
UPDATE Metadata SET data = replace(data, '"http://www.opengis.net/gml"', '"http://www.opengis.net/gml/3.2"') WHERE data LIKE '%"http://www.opengis.net/gml"%' AND schemaId LIKE 'iso19139%';

-- Unset 2005 schemaLocation
UPDATE Metadata SET data = replace(data, ' xsi:schemaLocation="http://www.isotc211.org/2005/gmd https://www.isotc211.org/2005/gmd/gmd.xsd http://www.isotc211.org/2005/gmx https://www.isotc211.org/2005/gmx/gmx.xsd http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd"', '') WHERE data LIKE '%xsi:schemaLocation="http://www.isotc211.org/2005/gmd https://www.isotc211.org/2005/gmd/gmd.xsd http://www.isotc211.org/2005/gmx https://www.isotc211.org/2005/gmx/gmx.xsd http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd%';

UPDATE Settings SET internal='n' WHERE name='system/server/securePort';


UPDATE metadata SET data = replace(data, '<gmd:version gco:nilReason="missing">', '<gmd:version gco:nilReason="unknown">') WHERE  data LIKE '%<gmd:version gco:nilReason="missing">%';


UPDATE Settings SET  position = position + 1 WHERE name = 'metadata/workflow/draftWhenInGroup';
UPDATE Settings SET  position = position + 1 WHERE name = 'metadata/workflow/allowPublishInvalidMd';
UPDATE Settings SET  position = position + 1 WHERE name = 'metadata/workflow/automaticUnpublishInvalidMd';
UPDATE Settings SET  position = position + 1 WHERE name = 'metadata/workflow/forceValidationOnMdSave';


-- geocat in INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/enable', 'true', 2, 100002, 'n');
-- geocat in INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/allowSumitApproveInvalidMd', 'true', 2, 100004, 'n');
-- geocat in INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/allowPublishNonApprovedMd', 'true', 2, 100005, 'n');



UPDATE Settings SET value='3.7.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


-- ## 3.8.1
-- ## 3.8.2

UPDATE Sources SET type = 'portal' WHERE type IS null AND uuid = (SELECT value FROM settings WHERE name = 'system/site/siteId');
UPDATE Sources SET type = 'harvester' WHERE type IS null AND uuid != (SELECT value FROM settings WHERE name = 'system/site/siteId');

UPDATE Settings SET internal = 'y' WHERE name = 'system/publication/doi/doipassword';

UPDATE Settings SET value='3.8.2' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


-- ## 3.8.3
-- ## 3.9.0
-- ## 3.10.1

DELETE FROM cswservercapabilitiesinfo;
DELETE FROM Settings WHERE name = 'system/csw/contactId';
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/capabilityRecordUuid', '-1', 0, 1220, 'y');

UPDATE Settings SET value='3.10.1' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- ## 3.10.2
-- ## 3.10.3

ALTER TABLE groupsdes ALTER COLUMN label TYPE varchar(255);
ALTER TABLE sourcesdes ALTER COLUMN label TYPE varchar(255);
ALTER TABLE schematrondes ALTER COLUMN label TYPE varchar(255);

-- New setting for server timezone
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/timeZone', 'Europe/Zurich', 0, 260, 'n');

-- keep these at the bottom of the file!
DROP INDEX idx_metadatafiledownloads_metadataid;
DROP INDEX idx_metadatafileuploads_metadataid;
DROP INDEX idx_operationallowed_metadataid;

UPDATE Settings SET value='3.10.3' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

-- ## 3.10.4
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/users/identicon', 'gravatar:mp', 0, 9110, 'n');

UPDATE Settings SET value='3.10.4' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

-- ## 3.11.0

-- Increase the length of Validation type (where the schematron file name is stored)
ALTER TABLE Validation ALTER COLUMN valType TYPE varchar(128);

ALTER TABLE usersearch ALTER COLUMN url TYPE text;

INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (63,'recordrestored','y', 63, 'event', null);
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'ara','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'cat','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'chi','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'dut','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'eng','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'fre','Fiche restaurée.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'fin','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'ger','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'ita','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'nor','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'pol','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'por','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'rus','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'slo','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'spa','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'tur','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'vie','Record restored.');


UPDATE Settings SET value='3.11.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


-- ## 4.0.0
DROP TABLE metadatanotifications;
DROP TABLE metadatanotifiers;

DELETE FROM Settings WHERE name LIKE 'system/indexoptimizer%';
DELETE FROM Settings WHERE name LIKE 'system/requestedLanguage%';
DELETE FROM Settings WHERE name = 'system/inspire/enableSearchPanel';
DELETE FROM Settings WHERE name = 'system/autodetect/enable';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/index/indexingTimeRecordLink', 'false', 2, 9209, 'n');

UPDATE metadata
SET data = REGEXP_REPLACE(data, '[a-z]{3}\/thesaurus\.download\?ref=', 'api/registries/vocabularies/', 'g')
WHERE data LIKE '%thesaurus.download?ref=%';

UPDATE settings SET value = '1' WHERE name = 'system/threadedindexing/maxthreads';

UPDATE Settings SET value='4.0.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- ## 4.0.1
-- ## 4.0.2

ALTER TABLE guf_userfeedbacks_guf_rating DROP COLUMN GUF_UserFeedbacks_uuid;

UPDATE Settings SET value='4.0.2' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- ## 4.0.3

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/passwordEnforcement/minLength', '6', 1, 12000, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/passwordEnforcement/maxLength', '20', 1, 12001, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/passwordEnforcement/usePattern', 'true', 2, 12002, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/passwordEnforcement/pattern', '^((?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*(_|[^\w])).*)$', 0, 12003, 'n');

UPDATE Settings SET value='4.0.3' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- ## 4.0.4

DELETE FROM Schematrondes WHERE iddes IN (SELECT id FROM schematron WHERE filename LIKE 'schematron-rules-inspire%');
DELETE FROM Schematroncriteria WHERE group_name || group_schematronid IN (SELECT name || schematronid FROM schematroncriteriagroup WHERE schematronid IN (SELECT id FROM schematron WHERE filename LIKE 'schematron-rules-inspire%'));
DELETE FROM Schematroncriteriagroup WHERE schematronid IN (SELECT id FROM schematron WHERE filename LIKE 'schematron-rules-inspire%');
DELETE FROM Schematron WHERE filename LIKE 'schematron-rules-inspire%';


ALTER TABLE Settings ADD COLUMN encrypted VARCHAR(1) DEFAULT 'n';
UPDATE Settings SET encrypted='y' WHERE name='system/proxy/password';
UPDATE Settings SET encrypted='y' WHERE name='system/feedback/mailServer/password';
UPDATE Settings SET encrypted='y' WHERE name='system/publication/doi/doipassword';

UPDATE Settings SET value='4.0.4' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


-- Geocat specific

-- SELECT * FROM settings WHERE name LIKE '%ui%';
UPDATE Settings SET value='default' WHERE name='system/ui/defaultView';

-- Virtual CSW migration
DELETE FROM sourcesdes
WHERE iddes not in (SELECT distinct(source) FROM metadata);

DELETE FROM sources
WHERE uuid not in (SELECT distinct(source) FROM metadata);


INSERT INTO sources (uuid, name, creationdate, filter, groupowner, logo, servicerecord, type, uiconfig)
SELECT replace(s.name, 'csw-', ''), replace(s.name, 'csw-', ''),
       '20210619', '+groupOwner:' || p.value,
       null, null, null, 'subportal', null
FROM services s LEFT JOIN serviceparameters p
                          ON s.id = p.service WHERE p.name = '_groupOwner';

INSERT INTO sources (uuid, name, creationdate, filter, groupowner, logo, servicerecord, type, uiconfig)
SELECT replace(s.name, 'csw-', ''), replace(s.name, 'csw-', ''),
       '20210619', '+source:' || p.value,
       null, null, null, 'subportal', null
FROM services s LEFT JOIN serviceparameters p
                          ON s.id = p.service WHERE p.name = '_source';


INSERT INTO sourcesdes (iddes, label, langid)
SELECT replace(s.name, 'csw-', ''), replace(s.name, 'csw-', ''), l.id
FROM services s, languages l;



DROP TABLE ServiceParameters;
DROP TABLE Services;

UPDATE settings SET value = 'true' WHERE name = 'system/inspire/enable';
UPDATE settings SET value = 'https://inspire.ec.europa.eu/validator/' WHERE name = 'system/inspire/remotevalidation/url';
UPDATE settings SET internal = 'n' WHERE name = 'system/metadata/prefergrouplogo';
-- ## 4.0.0 - After startup

-- Utility script to update sequence to current value on Postgres
-- https://github.com/geonetwork/core-geonetwork/pull/5003
create sequence serviceparameter_id_seq;
create sequence address_id_seq;
create sequence csw_server_capabilities_info_id_seq;
create sequence files_id_seq;
create sequence group_id_seq;
create sequence gufkey_id_seq;
create sequence gufrat_id_seq;
create sequence harvest_history_id_seq;
create sequence harvester_setting_id_seq;
create sequence inspire_atom_feed_id_seq;
create sequence iso_language_id_seq;
create sequence link_id_seq;
create sequence linkstatus_id_seq;
create sequence mapserver_id_seq;
create sequence metadata_category_id_seq;
create sequence metadata_filedownload_id_seq;
create sequence metadata_fileupload_id_seq;
create sequence metadata_id_seq;
create sequence metadata_identifier_template_id_seq;
create sequence operation_id_seq;
create sequence rating_criteria_id_seq;
create sequence schematron_criteria_id_seq;
create sequence schematron_id_seq;
create sequence selection_id_seq;
create sequence status_value_id_seq;
create sequence user_id_seq;
create sequence user_search_id_seq;
create sequence messageproducerentity_id_seq;
create sequence annotation_id_seq;
create sequence message_producer_entity_id_seq;
create sequence metadatastatus_id_seq;


-- Utility script to update sequence to current value on Postgres
-- https://github.com/geonetwork/core-geonetwork/pull/5003
SELECT setval('address_id_seq', (SELECT max(id) + 1 FROM address));
SELECT setval('csw_server_capabilities_info_id_seq', (SELECT max(idfield) FROM cswservercapabilitiesinfo));
SELECT setval('files_id_seq', (SELECT max(id) + 1 FROM files));
SELECT setval('group_id_seq', (SELECT max(id) + 1 FROM groups));
SELECT setval('gufkey_id_seq', (SELECT max(id) + 1 FROM guf_keywords));
SELECT setval('gufrat_id_seq', (SELECT max(id) + 1 FROM guf_rating));
SELECT setval('harvest_history_id_seq', (SELECT max(id) + 1 FROM harvesthistory));
SELECT setval('harvester_setting_id_seq', (SELECT max(id) + 1 FROM harvestersettings));
SELECT setval('inspire_atom_feed_id_seq', (SELECT max(id) + 1 FROM inspireatomfeed));
SELECT setval('iso_language_id_seq', (SELECT max(id) + 1 FROM isolanguages));
SELECT setval('link_id_seq', (SELECT max(id) + 1 FROM links));
SELECT setval('linkstatus_id_seq', (SELECT max(id) + 1 FROM linkstatus));
SELECT setval('mapserver_id_seq', (SELECT max(id) + 1 FROM mapservers));
SELECT setval('metadata_category_id_seq', (SELECT max(id) + 1 FROM categories));
SELECT setval('metadata_filedownload_id_seq', (SELECT max(id) + 1 FROM metadatafiledownloads));
SELECT setval('metadata_fileupload_id_seq', (SELECT max(id) + 1 FROM metadatafileuploads));
SELECT setval('metadata_id_seq', (SELECT max(id) + 1 FROM metadata));
SELECT setval('metadata_identifier_template_id_seq', (SELECT max(id) + 1 FROM metadataidentifiertemplate));
SELECT setval('operation_id_seq', (SELECT max(id) + 1 FROM operations));
SELECT setval('rating_criteria_id_seq', (SELECT max(id) + 1 FROM guf_ratingcriteria));
SELECT setval('schematron_criteria_id_seq', (SELECT max(id) + 1 FROM schematroncriteria));
SELECT setval('schematron_id_seq', (SELECT max(id) + 1 FROM schematron));
SELECT setval('selection_id_seq', (SELECT max(id) + 1 FROM selections));
SELECT setval('status_value_id_seq', (SELECT max(id) + 1 FROM statusvalues));
SELECT setval('user_id_seq', (SELECT max(id) + 1 FROM users));
SELECT setval('user_search_id_seq', (SELECT max(id) + 1 FROM usersearch));
