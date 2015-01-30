-- Create temporary tables used when modifying a column type

-- Convert Profile column to the profile enumeration ordinal

CREATE TABLE USERGROUPS_TMP 
(
   USERID int NOT NULL,
   GROUPID int NOT NULL,
   PROFILE int NOT NULL
);

-- Convert Profile column to the profile enumeration ordinal
-- GEOCAT
CREATE TABLE USERS_TMP
  (
    id integer NOT NULL,
    username text NOT NULL,
    password character varying(120) NOT NULL,
    surname text,
    name text,
    profile character varying(32) NOT NULL,
    address text,
    city text,
    state text,
    zip text,
    country text,
    email text,
    organisation text,
    kind character varying(16),
    security character varying(128) DEFAULT ''::character varying,
    authtype character varying(32),
    streetnumber text,
    streetname text,
    postbox text,
    positionname text,
    onlineresource text,
    onlinename text,
    onlinedescription text,
    hoursofservice text,
    contactinstructions text,
    publicaccess character(1) DEFAULT 'y'::bpchar,
    orgacronym text,
    directnumber text,
    mobile text,
    phone text,
    facsimile text,
    email1 text,
    phone1 text,
    facsimile1 text,
    email2 text,
    phone2 text,
    facsimile2 text,
    parentinfo integer,
    validated character(1) DEFAULT 'n'::bpchar
  );
-- END GEOCAT

-- ----  Change notifier actions column to map to the MetadataNotificationAction enumeration

CREATE TABLE MetadataNotifications_Tmp
  (
    metadataId         int            not null,
    notifierId         int            not null,
    notified           char(1)        default 'n' not null,
    metadataUuid       varchar(250)   not null,
    action             int            not null,
    errormsg           text
  );

-- ----  Change params querytype column to map to the LuceneQueryParamType enumeration

CREATE TABLE Params_TEMP
  (
    id          int           not null,
    requestId   int,
    queryType   int,
    termField   varchar(128),
    termText    varchar(128),
    similarity  float,
    lowerText   varchar(128),
    upperText   varchar(128),
    inclusive   char(1)
);
