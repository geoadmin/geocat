DELETE FROM MetadataCateg WHERE metadataid IN (SELECT id FROM Metadata WHERE isHarvested = 'y');
DELETE FROM operationallowed WHERE metadataid IN (SELECT id FROM Metadata WHERE isHarvested = 'y');
DELETE FROM validation WHERE metadataid IN (SELECT id FROM Metadata WHERE isHarvested = 'y');
DELETE FROM metadatafiledownloads WHERE id IN (SELECT id FROM Metadata WHERE isHarvested = 'y');
DELETE FROM metadatafileuploads WHERE id IN (SELECT id FROM Metadata WHERE isHarvested = 'y');
DELETE FROM Metadata WHERE isHarvested = 'y';


INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('system/csw/enabledWhenIndexing', 'true', 2, 1211, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('metadata/import/restrict', 'iso19139.che', 0, 11000, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('system/metadatacreate/generateUuid', 'true', 2, 9100, 'n');

INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('metadata/workflow/draftWhenInGroup', '', 0, 100002, 'n');
INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('system/oai/maxrecords', '10', 1, 7040, 'y');



ALTER TABLE HarvesterData RENAME COLUMN "key" TO keyvalue;


INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('metadata/workflow/allowPublishInvalidMd', 'true', 2, 100003, 'n');
INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('metadata/workflow/automaticUnpublishInvalidMd', 'true', 2, 100004, 'n');
INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('metadata/workflow/forceValidationOnMdSave', 'false', 2, 100005, 'n');



DELETE FROM Settings WHERE name = 'system/metadata/enableSimpleView';
DELETE FROM Settings WHERE name = 'system/metadata/enableIsoView';
DELETE FROM Settings WHERE name = 'system/metadata/enableInspireView';
DELETE FROM Settings WHERE name = 'system/metadata/enableXmlView';
DELETE FROM Settings WHERE name = 'system/metadata/defaultView';


INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('system/feedback/mailServer/tls', 'false', 2, 644, 'y');
INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('system/feedback/mailServer/ignoreSslCertificateErrors', 'false', 2, 645, 'y');
INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('system/xlinkResolver/ignore', 'operatesOn,featureCatalogueCitation,Anchor,source', 0, 2312, 'n');

DELETE FROM Settings WHERE name = 'system/z3950/enable';
DELETE FROM Settings WHERE name = 'system/z3950/port';
DELETE FROM Settings WHERE name = 'system/removedMetadata/dir';


UPDATE Settings SET datatype = 3 WHERE name = 'metadata/editor/schemaConfig';



INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('system/cors/allowedHosts', '*', 0, 561, 'y');
INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES
  ('map/bingKey', 'AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_p_0hkO1yo78ba8zTLARcLBIf8H6D', 0, 9595, 'n');



ALTER TABLE users ADD COLUMN isenabled CHAR(1) DEFAULT 'y';
UPDATE users SET isenabled = 'y';

ALTER TABLE groups ADD COLUMN enableCategoriesRestriction CHAR(1) DEFAULT 'n';
UPDATE groups SET enableCategoriesRestriction = 'n';


INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('ui/config', '{"langDetector":{"fromHtmlTag":false,"regexp":"^/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+/([a-z]{3})/","default":"eng"},"nodeDetector":{"regexp":"^/[a-zA-Z0-9_-]+/([a-zA-Z0-9_-]+)/[a-z]{3}/","default":"srv"},"mods":{"header":{"enabled":true,"languages":{"eng":"en","fre":"fr","ger":"ge","ita":"it"}},"home":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/home"},"search":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/search","hitsperpageValues":[10,50,100],"paginationInfo":{"hitsPerPage":20},"facetsSummaryType":"details","facetConfig":[],"facetTabField": "","filters":{},"sortbyValues":[{"sortBy":"relevance","sortOrder":""},{"sortBy":"changeDate","sortOrder":""},{"sortBy":"title","sortOrder":"reverse"},{"sortBy":"rating","sortOrder":""},{"sortBy":"popularity","sortOrder":""},{"sortBy":"denominatorDesc","sortOrder":""},{"sortBy":"denominatorAsc","sortOrder":"reverse"}],"sortBy":"relevance","resultViewTpls":[{"tplUrl":"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html","tooltip":"Grid","icon":"fa-th"}],"resultTemplate":"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html","formatter":{"list":[{"label":"full","url":"../api/records/{{uuid}}/formatters/full_view?output=xml"}]},"linkTypes":{"links":["LINK","kml"],"downloads":["DOWNLOAD"],"layers":["OGC"],"maps":["ows"]}},"map":{"enabled":false,"appUrl":"../../srv/{{lang}}/catalog.search#/map","is3DModeAllowed":false,"isSaveMapInCatalogAllowed":true,"bingKey":"AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_p_0hkO1yo78ba8zTLARcLBIf8H6D","storage":"sessionStorage","map":"../../map/config-viewer.xml","listOfServices":{"wms":[],"wmts":[]},"useOSM":true,"context":"","layer":{"url":"http://www2.demis.nl/mapserver/wms.asp?","layers":"Countries","version":"1.1.1"},"projection":"EPSG:3857","projectionList":[{"code":"EPSG:4326","label":"WGS84(EPSG:4326)"},{"code":"EPSG:3857","label":"Googlemercator(EPSG:3857)"},{"code":"EPSG:21781","label":"Swiss LV03(EPSG:21781)"}]},"editor":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.edit"},"admin":{"enabled":true,"appUrl":"../../srv/{{lang}}/admin.console"},"signin":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.signin"},"signout":{"appUrl":"../../signout"}}}', 3, 10000, 'n');

-- INSERT INTO Settings (name, value, datatype, position, internal) VALUES
--   ('metadata/editor/schemaConfig', '{"iso19110":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"readonly":true,"categories":["dataset"]},"validation":{"display":true}},"iso19139":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"categories":[]},"suggestion":{"display":true},"validation":{"display":true}},"iso19139.che":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"categories":["thumbnail", "onlinesrc", "service", "dataset", "source", "sibling"]},"suggestion":{"display":true},"validation":{"display":true}},"dublin-core":{"defaultTab":"default","related":{"display":true,"readonly":false,"categories":["parent","onlinesrc"]}}}', 3, 10000, 'n');

UPDATE Settings SET value = '{"iso19110":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"readonly":true,"categories":["dataset"]},"validation":{"display":true}},"iso19139":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"categories":[]},"suggestion":{"display":true},"validation":{"display":true}},"iso19139.che":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"categories":["thumbnail", "onlinesrc", "service", "dataset", "source", "sibling"]},"suggestion":{"display":true},"validation":{"display":true}},"dublin-core":{"defaultTab":"default","related":{"display":true,"readonly":false,"categories":["parent","onlinesrc"]}}}'
  WHERE name = 'metadata/editor/schemaConfig';

DELETE FROM Settings WHERE  name = 'map/is3DModeAllowed';
DELETE FROM Settings WHERE  name = 'map/isMapViewerEnabled';
DELETE FROM Settings WHERE  name = 'map/config';
DELETE FROM Settings WHERE  name = 'map/proj4js';
DELETE FROM Settings WHERE  name = 'map/isSaveMapInCatalogAllowed';
DELETE FROM Settings WHERE  name = 'map/bingKey';


CREATE TABLE selections
(
  id integer NOT NULL,
  name character varying(255) NOT NULL,
  iswatchable character(1) NOT NULL,
  CONSTRAINT selections_pkey PRIMARY KEY (id)
);

CREATE TABLE selectionsdes
(
  iddes integer NOT NULL,
  label character varying(255) NOT NULL,
  langid character varying(5) NOT NULL,
  CONSTRAINT selectionsdes_pkey PRIMARY KEY (iddes, langid)
);

INSERT INTO Selections (id, name, isWatchable) VALUES (0, 'PreferredList', 'n');
INSERT INTO Selections (id, name, isWatchable) VALUES (1, 'WatchList', 'y');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'ara','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'ara','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'cat','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'cat','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'chi','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'chi','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'dut','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'dut','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'eng','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'eng','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'fin','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'fin','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'fre','Fiches préférées');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'fre','Fiches observées');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'ger','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'ger','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'ita','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'ita','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'nor','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'nor','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'pol','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'pol','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'por','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'por','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'rus','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'rus','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'spa','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'spa','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'tur','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'tur','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'vie','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'vie','Watch list');

INSERT INTO Settings (name, value, datatype, position, internal)
  VALUES ('system/userFeedback/lastNotificationDate', '', 0, 1912, 'y');



-- New tables
CREATE TABLE public.metadataidentifiertemplate
(
  id integer NOT NULL,
  name character varying(32) NOT NULL,
  isprovided character(1) NOT NULL DEFAULT 'n'::bpchar,
  template character varying(255) NOT NULL,
  CONSTRAINT metadataidentifiertemplate_pkey PRIMARY KEY (id)
);

INSERT INTO MetadataIdentifierTemplate (id, name, template, isprovided) VALUES  (1, 'Autogenerated URN', ' ', 'y');




UPDATE Settings SET value='3.4.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';



-- Migrate resource.get links - Done in XSLT
-- SELECT link FROM (
--   SELECT unnest(xpath(
--             './/*[contains(text(), "resource.get"]',
--             data::xml)::varchar[]) AS link
--           FROM metadata
--           ORDER BY link
-- ) AS links;


-- TODO CGP
-- Remove CGP harvester for the time being.
DELETE FROM harvesterSettings WHERE id IN (
  SELECT id FROM harvesterSettings
    WHERE name = 'node' AND value = 'cgp'
  UNION
  SELECT id FROM harvesterSettings
    WHERE parentId IN (
    SELECT id FROM harvesterSettings WHERE name = 'node' AND value = 'cgp')
  UNION
  SELECT id FROM harvesterSettings
    WHERE parentId IN (
    SELECT id FROM harvesterSettings WHERE parentId = (
      SELECT id FROM harvesterSettings WHERE name = 'node' AND value = 'cgp'))
  UNION
  SELECT id FROM harvesterSettings
    WHERE parentId IN (
    SELECT id FROM harvesterSettings WHERE parentId IN (
      SELECT id FROM harvesterSettings WHERE parentId = (
        SELECT id FROM harvesterSettings WHERE name = 'node' AND value = 'cgp')))
  UNION
  SELECT id FROM harvesterSettings
    WHERE parentId IN (
    SELECT id FROM harvesterSettings WHERE parentId IN (
      SELECT id FROM harvesterSettings WHERE parentId IN (
        SELECT id FROM harvesterSettings WHERE parentId = (
          SELECT id FROM harvesterSettings WHERE name = 'node' AND value = 'cgp'))))
);



-- Create extent subtemplate for MGDI
-- Table: non_validated
INSERT INTO metadata(id, uuid, schemaId, istemplate, isHarvested, createdate, changedate, data, source, title, root, harvestuuid, owner, groupowner, harvesturi, rating, popularity, displayorder, doctype, extra) (
  SELECT
      nextval('hibernate_sequence') as id,
      concat('geocatch-subtpl-extent-non_validated-', id) AS uuid,
      'iso19139' AS schemaId,
      's' AS istemplate,
      'n' AS isHarvested,
      to_char(current_timestamp, 'YYYY-MM-DD"T"HH:MI:SS') AS createdate,
      to_char(current_timestamp, 'YYYY-MM-DD"T"HH:MI:SS') AS changedate,
      '<gmd:EX_Extent xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml">
        <gmd:description xsi:type="gmd:PT_FreeText_PropertyType">
         <gmd:PT_FreeText>
          <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#EN">' || endesc || '</gmd:LocalisedCharacterString>
          </gmd:textGroup>
          <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#DE">' || dedesc || '</gmd:LocalisedCharacterString>
          </gmd:textGroup>
          <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#FR">' || frdesc || '</gmd:LocalisedCharacterString>
          </gmd:textGroup>
          <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#IT">' || itdesc || '</gmd:LocalisedCharacterString>
          </gmd:textGroup>
          <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#RM">' || rmdesc || '</gmd:LocalisedCharacterString>
          </gmd:textGroup>
          </gmd:PT_FreeText>
        </gmd:description>
        <gmd:geographicElement>
          <gmd:EX_GeographicDescription>
            <gmd:geographicIdentifier>
              <gmd:MD_Identifier>
                <gmd:code xsi:type="gmd:PT_FreeText_PropertyType">
                  <gmd:PT_FreeText>
                  <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#EN">' || enid || '</gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                  <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#DE">' || deid || '</gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                  <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#FR">' || frid || '</gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                  <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#IT">' || itid || '</gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                  <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#RM">' || rmid || '</gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                  </gmd:PT_FreeText>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:geographicIdentifier>
          </gmd:EX_GeographicDescription>
        </gmd:geographicElement>
        <gmd:geographicElement>
          <gmd:EX_GeographicBoundingBox>
            <gmd:westBoundLongitude>
              <gco:Decimal>' || minx || '</gco:Decimal>
            </gmd:westBoundLongitude>
            <gmd:eastBoundLongitude>
              <gco:Decimal>' || maxx || '</gco:Decimal>
            </gmd:eastBoundLongitude>
            <gmd:southBoundLatitude>
              <gco:Decimal>' || miny || '</gco:Decimal>
            </gmd:southBoundLatitude>
            <gmd:northBoundLatitude>
              <gco:Decimal>' || maxy || '</gco:Decimal>
            </gmd:northBoundLatitude>
          </gmd:EX_GeographicBoundingBox>
        </gmd:geographicElement>
        <gmd:geographicElement>
          <gmd:EX_BoundingPolygon>
            <gmd:polygon> ' || gml || '</gmd:polygon>
          </gmd:EX_BoundingPolygon>
        </gmd:geographicElement>
      </gmd:EX_Extent>' AS data,
      (SELECT value FROM settings WHERE name = 'system/site/siteId') AS source,
      '' AS title,
      'gmd:EX_Extent' AS root,
      '' AS harvestuuid,
      (SELECT id FROM users WHERE name = 'admin' ORDER by 1 LIMIT 1) AS owner,
      (SELECT id FROM groups WHERE name = 'SUBTEMPLATES' ORDER by 1 LIMIT 1) AS groupowner,
      '' AS harvesturi,
      0 AS rating,
      0 AS popularity,
      null AS displayorder,
      null AS doctype,
      'nonvalidated' AS extra
       FROM (
    SELECT "ID"::varchar id, COALESCE((xpath(
      '//DE/text()',
      concat('<root>', replace(replace("GEO_ID", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS deid,
      COALESCE((xpath(
      '//FR/text()',
      concat('<root>', replace(replace("GEO_ID", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS frid,
      COALESCE((xpath(
      '//IT/text()',
      concat('<root>', replace(replace("GEO_ID", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS itid,
      COALESCE((xpath(
      '//RM/text()',
      concat('<root>', replace(replace("GEO_ID", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS rmid,
      COALESCE((xpath(
      '//EN/text()',
      concat('<root>', replace(replace("GEO_ID", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS enid,
      COALESCE((xpath(
      '//DE/text()',
      concat('<root>', replace(replace("DESC", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS dedesc,
      COALESCE((xpath(
      '//FR/text()',
      concat('<root>', replace(replace("DESC", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS frdesc,
      COALESCE((xpath(
      '//IT/text()',
      concat('<root>', replace(replace("DESC", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS itdesc,
      COALESCE((xpath(
      '//RM/text()',
      concat('<root>', replace(replace("DESC", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS rmdesc,
      COALESCE((xpath(
      '//EN/text()',
      concat('<root>', replace(replace("DESC", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS endesc,
      replace(
        replace(
          replace(
            ST_AsGML(3, (ST_Transform(ST_Force2D(the_geom), 4326)), 6, 0),
            '<gml:MultiSurface',
            concat('<gml:MultiSurface gml:id="_ms', "ID", floor(random() * 10000000000), '"')
          ),
          '<gml:Polygon',
          concat('<gml:Polygon gml:id="_p', "ID", floor(random() * 10000000000), '"')
        ),
        'srsName="EPSG:4326"',
        ''
      ) gml,
      ST_XMin(ST_Transform(the_geom, 4326)) AS minx,
      ST_YMin(ST_Transform(the_geom, 4326)) AS miny,
      ST_XMax(ST_Transform(the_geom, 4326)) AS maxx,
      ST_YMax(ST_Transform(the_geom, 4326)) AS maxy
      FROM non_validated
      -- list of duplicate ids (taken from a sql query)
      WHERE "ID" not in (3,4,5,6,7,8,10,18,41,43,44,45,46,47,48,53,54,56,57,61,63,64,66,67,69,71,72,73,74,75,77,79,80,81,82,84,85,87,88,89,91,93,94,96,98,103,105,106,110,112,115,55,58,70,101,108,113,62,76,78,83,90,92,95,99)
      ) AS extent
);


-- Table: xlinks
INSERT INTO metadata(id, uuid, schemaId, istemplate, isHarvested, createdate, changedate, data, source, title, root, harvestuuid, owner, groupowner, harvesturi, rating, popularity, displayorder, doctype, extra) (
  SELECT
      nextval('hibernate_sequence') as id,
      concat('geocatch-subtpl-extent-custom-', id) AS uuid,
      'iso19139' AS schemaId,
      's' AS istemplate,
      'n' AS isHarvested,
      to_char(current_timestamp, 'YYYY-MM-DD"T"HH:MI:SS') AS createdate,
      to_char(current_timestamp, 'YYYY-MM-DD"T"HH:MI:SS') AS changedate,
      '<gmd:EX_Extent xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml">
        <gmd:description xsi:type="gmd:PT_FreeText_PropertyType">
         <gmd:PT_FreeText>
          <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#EN">' || endesc || '</gmd:LocalisedCharacterString>
          </gmd:textGroup>
          <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#DE">' || dedesc || '</gmd:LocalisedCharacterString>
          </gmd:textGroup>
          <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#FR">' || frdesc || '</gmd:LocalisedCharacterString>
          </gmd:textGroup>
          <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#IT">' || itdesc || '</gmd:LocalisedCharacterString>
          </gmd:textGroup>
          <gmd:textGroup>
          <gmd:LocalisedCharacterString locale="#RM">' || rmdesc || '</gmd:LocalisedCharacterString>
          </gmd:textGroup>
          </gmd:PT_FreeText>
        </gmd:description>
        <gmd:geographicElement>
          <gmd:EX_GeographicDescription>
            <gmd:geographicIdentifier>
              <gmd:MD_Identifier>
                <gmd:code xsi:type="gmd:PT_FreeText_PropertyType">
                  <gmd:PT_FreeText>
                  <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#EN">' || enid || '</gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                  <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#DE">' || deid || '</gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                  <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#FR">' || frid || '</gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                  <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#IT">' || itid || '</gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                  <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#RM">' || rmid || '</gmd:LocalisedCharacterString>
                  </gmd:textGroup>
                  </gmd:PT_FreeText>
                </gmd:code>
              </gmd:MD_Identifier>
            </gmd:geographicIdentifier>
          </gmd:EX_GeographicDescription>
        </gmd:geographicElement>
        <gmd:geographicElement>
          <gmd:EX_GeographicBoundingBox>
            <gmd:westBoundLongitude>
              <gco:Decimal>' || minx || '</gco:Decimal>
            </gmd:westBoundLongitude>
            <gmd:eastBoundLongitude>
              <gco:Decimal>' || maxx || '</gco:Decimal>
            </gmd:eastBoundLongitude>
            <gmd:southBoundLatitude>
              <gco:Decimal>' || miny || '</gco:Decimal>
            </gmd:southBoundLatitude>
            <gmd:northBoundLatitude>
              <gco:Decimal>' || maxy || '</gco:Decimal>
            </gmd:northBoundLatitude>
          </gmd:EX_GeographicBoundingBox>
        </gmd:geographicElement>
        <gmd:geographicElement>
          <gmd:EX_BoundingPolygon>
            <gmd:polygon> ' || gml || '</gmd:polygon>
          </gmd:EX_BoundingPolygon>
        </gmd:geographicElement>
      </gmd:EX_Extent>' AS data,
      (SELECT value FROM settings WHERE name = 'system/site/siteId') AS source,
      '' AS title,
      'gmd:EX_Extent' AS root,
      '' AS harvestuuid,
      (SELECT id FROM users WHERE name = 'admin' ORDER by 1 LIMIT 1) AS owner,
      (SELECT id FROM groups WHERE name = 'SUBTEMPLATES' ORDER by 1 LIMIT 1) AS groupowner,
      '' AS harvesturi,
      0 AS rating,
      0 AS popularity,
      null AS displayorder,
      null AS doctype,
      'validated' AS extra
       FROM (
    SELECT "ID"::varchar id, COALESCE((xpath(
      '//DE/text()',
      concat('<root>', replace(replace("GEO_ID", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS deid,
      COALESCE((xpath(
      '//FR/text()',
      concat('<root>', replace(replace("GEO_ID", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS frid,
      COALESCE((xpath(
      '//IT/text()',
      concat('<root>', replace(replace("GEO_ID", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS itid,
      COALESCE((xpath(
      '//RM/text()',
      concat('<root>', replace(replace("GEO_ID", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS rmid,
      COALESCE((xpath(
      '//EN/text()',
      concat('<root>', replace(replace("GEO_ID", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS enid,
      COALESCE((xpath(
      '//DE/text()',
      concat('<root>', replace(replace("DESC", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS dedesc,
      COALESCE((xpath(
      '//FR/text()',
      concat('<root>', replace(replace("DESC", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS frdesc,
      COALESCE((xpath(
      '//IT/text()',
      concat('<root>', replace(replace("DESC", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS itdesc,
      COALESCE((xpath(
      '//RM/text()',
      concat('<root>', replace(replace("DESC", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS rmdesc,
      COALESCE((xpath(
      '//EN/text()',
      concat('<root>', replace(replace("DESC", ']]>', ''), '<![CDATA[', ''), '</root>')::xml)::varchar[])[1], '') AS endesc,
      replace(
        replace(
          replace(
            ST_AsGML(3, (ST_Transform(ST_Force2D(the_geom), 4326)), 6, 0),
            '<gml:MultiSurface',
            concat('<gml:MultiSurface gml:id="_ms', "ID", floor(random() * 10000000000), '"')
          ),
          '<gml:Polygon',
          concat('<gml:Polygon gml:id="_p', "ID", floor(random() * 10000000000), '"')
        ),
        'srsName="EPSG:4326"',
        ''
      ) gml,
      ST_XMin(ST_Transform(the_geom, 4326)) AS minx,
      ST_YMin(ST_Transform(the_geom, 4326)) AS miny,
      ST_XMax(ST_Transform(the_geom, 4326)) AS maxx,
      ST_YMax(ST_Transform(the_geom, 4326)) AS maxy
      FROM xlinks
      ) AS extent
);



-- Report validation status from extra column to validation table for subtemplate
INSERT INTO validation(metadataid, valtype, status, tested, failed, valdate, required)
  SELECT id, 'subtemplate', 1, 0, 0, createdate, true
    FROM metadata WHERE isTemplate = 's' AND extra = 'validated';


INSERT INTO validation(metadataid, valtype, status, tested, failed, valdate, required)
  SELECT id, 'subtemplate', 0, 0, 0, createdate, true
    FROM metadata WHERE isTemplate = 's' AND extra = 'nonvalidated' AND
        id not in (SELECT metadataid FROM validation WHERE valtype = 'subtemplate');



-- Set publish to all for all validated subtemplates
-- In old geocat, subtemplates were only published to intranet and 9999
INSERT INTO operationallowed
  SELECT 1, id, 0
    FROM metadata WHERE isTemplate = 's' AND extra = 'validated';


-- Set edit privileges to SUBTEMPLATES group
INSERT INTO operationallowed
  SELECT (SELECT id FROM groups WHERE name = 'SUBTEMPLATES' ORDER by 1 LIMIT 1),
        id, 2
    FROM metadata WHERE uuid like 'geocatch-subtpl-extent-custom-%';



DELETE FROM Settings WHERE name like 'system/shib%';

UPDATE Metadata SET data = replace(data, 'http://www.geocat.ch/geonetwork/srv/eng//csw', 'http://www.geocat.ch/geonetwork/srv/eng/csw')
  WHERE data LIKE '%http://www.geocat.ch/geonetwork/srv/eng//csw%';

UPDATE Metadata SET data = replace(data, 'local._none_.geocat.ch', 'local.theme.geocat.ch')
  WHERE data LIKE '%local._none_.geocat.ch%';

UPDATE Metadata SET data = replace(data, 'external._none_.gemet', 'external.theme.gemet')
  WHERE data LIKE '%external._none_.gemet%';


-- TODO publish_tracking


-- XLink migration
-- SELECT xlink FROM (
--   SELECT unnest(xpath(
--             './/@xlink:href',
--             data::xml,
--             ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink']])::varchar[]) AS xlink
--           FROM metadata
--           ORDER BY xlink
-- ) AS xlinks
--   WHERE
--     xlink NOT LIKE 'https://%thesaurus.download%' AND
--     xlink NOT LIKE '%GetRecordById%' AND xlink like '%gn:xlinks%' AND xlink like '%.0&amp;%';

UPDATE metadata
  SET data = replace(data, 'skipdescriptivekeywords=true&amp;', '')
  WHERE data like '%skipdescriptivekeywords=true&amp;%';

DELETE FROM selectionsdes;
DELETE FROM Selections;


UPDATE metadata SET data = replace (data, 'xlink:show="replace"', '') WHERE data like '%xlink:show="replace"%';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/backuparchive/enable', 'true', 2, 12000, 'n');


DROP TABLE non_validated;
DROP TABLE hiddenmetadataelements;
DROP TABLE deletedobjects;
DROP TABLE geom_table_lastmodified;
DROP TABLE countries;
DROP TABLE countries_search;
DROP TABLE "countriesBB";
DROP TABLE "gemeindenBB";
DROP TABLE "kantoneBB";
DROP TABLE kantone_search;
DROP TABLE gemeinden_search;
DROP TABLE xlinks;



INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/referencedDeletionAllowed', 'true', 2, 2313, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/templatesToOperateOnAtInsert', 'keyword;contact;extent;format', 0, 2314, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/recaptcha/enable', 'false', 2, 1910, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/recaptcha/publickey', '', 0, 1910, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/recaptcha/secretkey', '', 0, 1910, 'y');


-- TODO Search statistics to drop or move to ES
-- DROP TABLE params;
-- DROP TABLE params_temp;
-- DROP TABLE params_v2;
-- DROP TABLE requests;
-- DROP TABLE requests_v2;
DELETE FROM Settings WHERE  name = 'metadata/editor/schemaConfig';

UPDATE harvestersettings SET value = 'GM03-to-ISO19139CHE' WHERE value = '/srv/tomcat/geocat/webapps/geonetwork/xsl/conversion/import/GM03-to-ISO19139CHE.xsl';



UPDATE metadata SET data = replace(data, 'file:///srv/tomcat/geocat/private/geocat/config/codelist/external/thesauri/theme/theme/', 'http%3A%2F%2Fwww.eionet.europa.eu%2Fgemet%2Ftheme%2F') where data like '%file:///srv/tomcat/geocat/private/geocat/config/codelist/external/thesauri/theme/theme/%';

update groups set logo = concat(logo, '.png')  where logo !='' and id < 50000000;
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/validation/removeSchemaLocation', 'true', 2, 9170, 'n');

UPDATE schematroncriteria SET value = uivalue WHERE type = 'XPATH' and uivalue != '';

-- add missing source label
INSERT INTO sourcesdes (iddes, langid, label) VALUES ('7ea582d4-9ddf-422e-b28f-29760a4c0147','eng','~geocat.ch direct partners');
INSERT INTO sourcesdes (iddes, langid, label) VALUES ('7ea582d4-9ddf-422e-b28f-29760a4c0147','roh','~geocat.ch direct partners');
INSERT INTO sourcesdes (iddes, langid, label) VALUES ('7ea582d4-9ddf-422e-b28f-29760a4c0147','ger','~geocat.ch direct partners');
INSERT INTO sourcesdes (iddes, langid, label) VALUES ('7ea582d4-9ddf-422e-b28f-29760a4c0147','ita','~geocat.ch direct partners');
INSERT INTO sourcesdes (iddes, langid, label) VALUES ('7ea582d4-9ddf-422e-b28f-29760a4c0147','fre','~geocat.ch direct partners');

-- inserting missing group desc
INSERT INTO groupsdes (iddes, label, langid) values (-1, 'Guest', 'ger');
INSERT INTO groupsdes (iddes, label, langid) values (-1, 'Guest', 'fre');
INSERT INTO groupsdes (iddes, label, langid) values (0, 'Intranet', 'ger');
INSERT INTO groupsdes (iddes, label, langid) values (0, 'Intranet', 'fre');
INSERT INTO groupsdes (iddes, label, langid) values (1, 'All', 'ger');
INSERT INTO groupsdes (iddes, label, langid) values (1, 'All', 'fre');
INSERT INTO groupsdes (iddes, label, langid) values (2, 'Sample Group', 'ger');
INSERT INTO groupsdes (iddes, label, langid) values (2, 'Sample Group', 'fre');

-- drop spatial index table (this will be recreated)
DROP TABLE spatialindex;

