UPDATE Settings SET value='3.0.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- GEOCAT
ALTER TABLE groups RENAME logouuid TO logo;
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/background', 'geocat', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/width', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/summaryWidth', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/mapproj', 'EPSG:21781', 0, 9590, 'n');
INSERT INTO groups(id, name, description, email, referrer, logo, website) VALUES (-1, 'GUEST', 'Reserved Guest Group', '', NULL, '', '');
INSERT INTO groupsdes(iddes, langid, label) VALUES (-1, 'eng', 'Guest');
INSERT INTO groupsdes(iddes, langid, label) VALUES (-1, 'fre', 'Invité');
INSERT INTO groupsdes(iddes, langid, label) VALUES (-1, 'ita', 'Ospite');
INSERT INTO groupsdes(iddes, langid, label) VALUES (-1, 'ger', 'Gast');
INSERT INTO groupsdes(iddes, langid, label) VALUES (-1, 'roh', '');
-- END GEOCAT

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/ignorehostlist', NULL, 0, 560, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atom', 'disabled', 0, 7230, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomSchedule', '0 0 0/24 ? * *', 0, 7240, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomProtocol', 'INSPIRE-ATOM', 0, 7250, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/prefergrouplogo', 'y', 0, 9111, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('map/isMapViewerEnabled', 'true', 2, 9592, 'n');
