UPDATE Settings SET value='3.0.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- GEOCAT
CREATE TABLE geom_table_lastmodified (
  name varchar(40),
  lastmodified timestamp,
  PRIMARY KEY(name)
);
INSERT INTO geom_table_lastmodified VALUES ('countries', now());
INSERT INTO geom_table_lastmodified VALUES ('countriesBB', now());
INSERT INTO geom_table_lastmodified VALUES ('countries_search', now());
INSERT INTO geom_table_lastmodified VALUES ('gemeindenBB', now());
INSERT INTO geom_table_lastmodified VALUES ('gemeinden_search', now());
INSERT INTO geom_table_lastmodified VALUES ('kantoneBB', now());
INSERT INTO geom_table_lastmodified VALUES ('kantone_search', now());
INSERT INTO geom_table_lastmodified VALUES ('non_validated', now());
INSERT INTO geom_table_lastmodified VALUES ('xlinks', now());

CREATE FUNCTION update_geom_lastmodified() RETURNS trigger AS $$
  BEGIN
    UPDATE geom_table_lastmodified SET lastmodified = now() WHERE name = TG_TABLE_NAME;
    RETURN NULL;
  END
$$ LANGUAGE plpgsql;

CREATE TRIGGER lastmodified_updater AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE ON countries EXECUTE PROCEDURE update_geom_lastmodified();
CREATE TRIGGER lastmodified_updater AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE ON "countriesBB" EXECUTE PROCEDURE update_geom_lastmodified();
CREATE TRIGGER lastmodified_updater AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE ON countries_search EXECUTE PROCEDURE update_geom_lastmodified();
CREATE TRIGGER lastmodified_updater AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE ON "gemeindenBB" EXECUTE PROCEDURE update_geom_lastmodified();
CREATE TRIGGER lastmodified_updater AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE ON gemeinden_search EXECUTE PROCEDURE update_geom_lastmodified();
CREATE TRIGGER lastmodified_updater AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE ON "kantoneBB" EXECUTE PROCEDURE update_geom_lastmodified();
CREATE TRIGGER lastmodified_updater AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE ON kantone_search EXECUTE PROCEDURE update_geom_lastmodified();
CREATE TRIGGER lastmodified_updater AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE ON non_validated EXECUTE PROCEDURE update_geom_lastmodified();
CREATE TRIGGER lastmodified_updater AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE ON xlinks EXECUTE PROCEDURE update_geom_lastmodified();

ALTER TABLE groups RENAME logouuid TO logo;
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/background', 'http://wms.geo.admin.ch/?SERVICE=WMS&REQUEST=GetMap&VERSION=1.1.0&LAYERS=ch.swisstopo.pixelkarte-farbe-pk1000.noscale&STYLES=default&SRS={srs}&BBOX={minx},{miny},{maxx},{maxy}&WIDTH={width}&HEIGHT={height}&FORMAT=image/png', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/width', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/summaryWidth', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/mapproj', 'EPSG:21781', 0, 9590, 'n');
INSERT INTO groups(id, name, description, email, referrer, logo, website) VALUES (-1, 'GUEST', 'Reserved Guest Group', '', NULL, '', '');
INSERT INTO groupsdes(iddes, langid, label) VALUES (-1, 'eng', 'Guest');
INSERT INTO groupsdes(iddes, langid, label) VALUES (-1, 'fre', 'Invité');
INSERT INTO groupsdes(iddes, langid, label) VALUES (-1, 'ita', 'Ospite');
INSERT INTO groupsdes(iddes, langid, label) VALUES (-1, 'ger', 'Gast');
INSERT INTO groupsdes(iddes, langid, label) VALUES (-1, 'roh', '');

DELETE FROM validation;
-- END GEOCAT

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/proxy/ignorehostlist', NULL, 0, 560, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atom', 'disabled', 0, 7230, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomSchedule', '0 0 0/24 ? * *', 0, 7240, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/atomProtocol', 'INSPIRE-ATOM', 0, 7250, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/prefergrouplogo', 'true', 2, 9111, 'y');

-- GEOCAT
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('map/isMapViewerEnabled', 'false', 2, 9592, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/allThesaurus', 'false', 2, 9160, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/ui/defaultView', 'geocat', 0, 10100, 'n');
UPDATE Settings
SET
  value='{"iso19110":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"readonly":true,"categories":["dataset"]},"validation":{"display":true}},"iso19139":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"categories":[]},"suggestion":{"display":true},"validation":{"display":true}},"dublin-core":{"defaultTab":"default","related":{"display":true,"readonly":false,"categories":["parent","onlinesrc"]}},"iso19139.che":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"categories":["thumbnail","onlinesrc","service","dataset","source","sibling"]},"suggestion":{"display":true},"validation":{"display":true}}}'
WHERE
  name = 'metadata/editor/schemaConfig';
-- END GEOCAT
INSERT INTO settings (name, value, datatype, position, internal) VALUES ('system/server/log','log4j',0,250,'y');
