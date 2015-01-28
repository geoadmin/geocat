UPDATE Settings SET value='3.0.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- GEOCAT
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/background', 'geocat', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/width', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/summaryWidth', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/mapproj', 'EPSG:21781', 0, 9590, 'n');

INSERT INTO metadata VALUES (-1, 'formatTemplate', 'iso19139.che','s','n','2009-12-09T22:04:36', '2013-11-20T15:36:33', '<gmd:MD_Format xmlns:gmd="http://www.isotc211.org/2005/gmd">
  <gmd:name>
    <gco:CharacterString xmlns:gco="http://www.isotc211.org/2005/gco">--</gco:CharacterString>
  </gmd:name>
  <gmd:version>
    <gco:CharacterString xmlns:gco="http://www.isotc211.org/2005/gco">--</gco:CharacterString>
  </gmd:version>
</gmd:MD_Format>', '7ea582d4-9ddf-422e-b28f-29760a4c0147', '', 'gmd:MD_Format', NULL, 1, NULL, NULL, 0, 0, 0, NULL, 'nonvalidated');

INSERT INTO metadata VALUES (-2, 'contactTemplate', 'iso19139.che','s','n','2009-12-09T22:04:36', '2013-11-20T15:36:33', '<che:CHE_CI_ResponsibleParty xmlns:che="http://www.geocat.ch/2008/che" xmlns:gco="http://www.isotc211.org/2005/gco" gco:isoType="gmd:CI_ResponsibleParty">
  <gmd:contactInfo xmlns:gmd="http://www.isotc211.org/2005/gmd">
  </gmd:contactInfo>
</che:CHE_CI_ResponsibleParty>', '7ea582d4-9ddf-422e-b28f-29760a4c0147', '', 'che:CHE_CI_ResponsibleParty', NULL, 1, NULL, NULL, 0, 0, 0, NULL, 'nonvalidated');
-- END GEOCAT

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/ignorehostlist', NULL, 0, 560, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atom', 'disabled', 0, 7230, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomSchedule', '0 0 0/24 ? * *', 0, 7240, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomProtocol', 'INSPIRE-ATOM', 0, 7250, 'y');
