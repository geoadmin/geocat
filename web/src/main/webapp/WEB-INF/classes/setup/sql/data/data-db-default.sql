

-- ======================================================================
-- === Table: Categories
-- ======================================================================

INSERT INTO Categories (id, name) VALUES (1,'maps');
INSERT INTO Categories (id, name) VALUES (2,'datasets');
INSERT INTO Categories (id, name) VALUES (3,'interactiveResources');
INSERT INTO Categories (id, name) VALUES (4,'applications');
INSERT INTO Categories (id, name) VALUES (5,'caseStudies');
INSERT INTO Categories (id, name) VALUES (6,'proceedings');
INSERT INTO Categories (id, name) VALUES (7,'photo');
INSERT INTO Categories (id, name) VALUES (8,'audioVideo');
INSERT INTO Categories (id, name) VALUES (9,'directories');
INSERT INTO Categories (id, name) VALUES (10,'otherResources');
INSERT INTO Categories (id, name) VALUES (11,'z3950Servers');
INSERT INTO Categories (id, name) VALUES (12,'registers');
INSERT INTO Categories (id, name) VALUES (13,'physicalSamples');

-- ======================================================================
-- === Table: Groups
-- ======================================================================

INSERT INTO Groups (id, name, description, email, referrer) VALUES (-1,'GUEST','self-registered users',NULL,NULL);
INSERT INTO Groups (id, name, description, email, referrer) VALUES (0,'intranet',NULL,NULL,NULL);
INSERT INTO Groups (id, name, description, email, referrer) VALUES (1,'all',NULL,NULL,NULL);
INSERT INTO Groups (id, name, description, email, referrer) VALUES (2,'sample',NULL,NULL,NULL);


-- ======================================================================
-- === Table: IsoLanguages (id, code, shortcode)
-- ======================================================================


-- ======================================================================
-- === Table: StatusValues
-- ======================================================================

INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (0,'unknown','y', 0);
INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (1,'draft','y', 1);
INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (2,'approved','y', 3);
INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (3,'retired','y', 5);
INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (4,'submitted','y', 2);
INSERT INTO StatusValues (id, name, reserved, displayorder) VALUES  (5,'rejected','y', 4);

-- ======================================================================
-- === Table: StatusValuesDes
-- ======================================================================

-- ======================================================================
-- === Table: Operations
-- ======================================================================

INSERT INTO Operations (id, name) VALUES  (0,'view');
INSERT INTO Operations (id, name) VALUES  (1,'download');
INSERT INTO Operations (id, name) VALUES  (2,'editing');
INSERT INTO Operations (id, name) VALUES  (3,'notify');
INSERT INTO Operations (id, name) VALUES  (5,'dynamic');
INSERT INTO Operations (id, name) VALUES  (6,'featured');


-- ======================================================================
-- === Table: Settings
-- ======================================================================

INSERT INTO Settings (name, value, datatype, position) VALUES ('system/site/name', 'My GeoNetwork catalogue', 0, 110);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/site/siteId', 'Dummy', 0, 120);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/site/organization', 'My organization', 0, 130);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/platform/version', '2.11.0', 0, 150);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/platform/subVersion', 'SNAPSHOT', 0, 160);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/site/svnUuid', '', 0, 170);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/server/host', 'localhost', 0, 210);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/server/port', '8190', 1, 220);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/server/protocol', 'http', 0, 230);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/server/securePort', '8493', 1, 240);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/intranet/network', '127.0.0.1', 0, 310);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/intranet/netmask', '255.0.0.0', 0, 320);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/z3950/enable', 'true', 2, 410);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/z3950/port', '2100', 1, 420);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/proxy/use', 'false', 2, 510);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/proxy/host', NULL, 0, 520);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/proxy/port', NULL, 1, 530);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/proxy/username', NULL, 0, 540);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/proxy/password', NULL, 0, 550);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/feedback/email', NULL, 0, 610);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/feedback/mailServer/host', '', 0, 630);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/feedback/mailServer/port', '25', 1, 640);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/feedback/mailServer/username', '', 0, 642);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/feedback/mailServer/password', '', 0, 643);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/feedback/mailServer/ssl', 'false', 2, 641);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/removedMetadata/dir', 'WEB-INF/data/removed', 0, 710);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/selectionmanager/maxrecords', '1000', 1, 910);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/csw/enable', 'true', 2, 1210);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/csw/contactId', NULL, 0, 1220);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/csw/metadataPublic', 'false', 2, 1310);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/shib/use', 'false', 2, 1710);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/shib/path', '/geonetwork/srv/en/shib.user.login', 0, 1720);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/shib/username', 'REMOTE_USER', 0, 1740);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/shib/surname', 'Shib-Person-surname', 0, 1750);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/shib/firstname', 'Shib-InetOrgPerson-givenName', 0, 1760);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/shib/profile', 'Shib-EP-Entitlement', 0, 1770);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/userSelfRegistration/enable', 'false', 2, 1910);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/clickablehyperlinks/enable', 'true', 2, 2010);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/localrating/enable', 'false', 2, 2110);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/downloadservice/leave', 'false', 0, 2210);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/downloadservice/simple', 'true', 0, 2220);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/downloadservice/withdisclaimer', 'false', 0, 2230);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/xlinkResolver/enable', 'false', 2, 2310);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/autofixing/enable', 'true', 2, 2410);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/searchStats/enable', 'true', 2, 2510);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/indexoptimizer/enable', 'true', 2, 6010);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/indexoptimizer/at/hour', '0', 1, 6030);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/indexoptimizer/at/min', '0', 1, 6040);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/indexoptimizer/at/sec', '0', 1, 6050);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/indexoptimizer/interval', NULL, 0, 6060);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/indexoptimizer/interval/day', '0', 1, 6070);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/indexoptimizer/interval/hour', '24', 1, 6080);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/indexoptimizer/interval/min', '0', 1, 6090);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/oai/mdmode', '1', 0, 7010);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/oai/tokentimeout', '3600', 1, 7020);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/oai/cachesize', '60', 1, 7030);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/inspire/enable', 'false', 2, 7210);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/inspire/enableSearchPanel', 'false', 2, 7220);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/harvester/enableEditing', 'false', 2, 9010);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/harvesting/mail/recipient', NULL, 0, 9020);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/harvesting/mail/template', '', 0, 9021);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/harvesting/mail/templateError', 'There was an error on the harvesting: $$errorMsg$$', 0, 9022);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/harvesting/mail/templateWarning', '', 0, 9023);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/harvesting/mail/subject', '[$$harvesterType$$] $$harvesterName$$ finished harvesting', 0, 9024);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/harvesting/mail/enabled', 'false', 2, 9025);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/harvesting/mail/level1', 'false', 2, 9026);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/harvesting/mail/level2', 'false', 2, 9027);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/harvesting/mail/level3', 'false', 2, 9028);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/metadata/enableSimpleView', 'true', 2, 9110);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/metadata/enableIsoView', 'true', 2, 9120);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/metadata/enableInspireView', 'false', 2, 9130);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/metadata/enableXmlView', 'true', 2, 9140);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/metadata/defaultView', 'simple', 0, 9150);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/metadataprivs/usergrouponly', 'false', 2, 9180);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/threadedindexing/maxthreads', '1', 1, 9210);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/autodetect/enable', 'false', 2, 9510);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/requestedLanguage/only', 'prefer_locale', 0, 9530);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/requestedLanguage/sorted', 'false', 2, 9540);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/hidewithheldelements/enable', 'false', 2, 9570);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/hidewithheldelements/keepMarkedElement', 'true', 2, 9580);
INSERT INTO Settings (name, value, datatype, position) VALUES ('system/lucene/ignorechars',E'-/\\_,.\'"', 0, 9999);

INSERT INTO HarvesterSettings (id, parentid, name, value) VALUES  (1,NULL,'harvesting',NULL);
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  (1002,        1, 'node'                , 'webdav')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1003,     1002, 'site'                , NULL)
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1004,     1002, 'options'             , NULL)
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1005,     1002, 'info'                , NULL)
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1006,     1002, 'content'             , NULL)
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1007,     1003, 'name'                , 'Kanton Zürich')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1008,     1003, 'uuid'                , '84be1273-ba53-469a-8a86-8b7e6107128e')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1009,     1003, 'useAccount'          , 'true')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1010,     1009, 'username'            , 'geocat_ZH')
INSERT INTO HarvesterSettings (id, parentid, name, value)  (id, parentid, name, value) sers VALUES  ( 1011,     1009, 'password'            , 'ZhfFsHoA')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1012,     1004, 'every'               , '90')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1013,     1004, 'oneRunOnly'          , 'false')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1014,     1004, 'status'              , 'inactive')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1015,     1006, 'importxslt'          , 'none')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1016,     1006, 'validate'            , 'false')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1017,     1005, 'lastRun'             , NULL)
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1018,     1002, 'privileges'          , NULL)
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1019,     1018, 'group'               , '1')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1020,     1019, 'operation'           , '0')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1021,     1002, 'categories'          , NULL)
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1022,     1003, 'url'                 , 'https://dav0.bgdi.admin.ch/geocat_ZH')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1023,     1003, 'icon'                , 'KtZH.gif')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1024,     1004, 'validate'            , 'false')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1025,     1004, 'recurse'             , 'false')
INSERT INTO HarvesterSettings (id, parentid, name, value)  VALUES  ( 1026,     1004, 'subtype', 'webdav')

-- ======================================================================
-- === Table: Users
-- ======================================================================

INSERT INTO Users (id, username, password, name, surname, profile, kind, organisation, security, authtype) VALUES  (1,'admin','46e44386069f7cf0d4f2a420b9a2383a612f316e2024b0fe84052b0b96c479a23e8a0be8b90fb8c2','admin','admin',0,'','','','');
INSERT INTO Address (id, address, city, country, state, zip) VALUES  (1, '', '', '', '', '');
INSERT INTO UserAddress (userid, addressid) VALUES  (1, 1);
