-- Drop the old tables (that are being migrated to an enum) and create them again with new definition

-- Update UserGroups profiles to be one of the enumerated profiles

TRUNCATE TABLE USERGROUPS;
ALTER TABLE USERGROUPS DROP COLUMN profile;
ALTER TABLE USERGROUPS ADD COLUMN profile integer NOT NULL;
-- Update UserGroups profiles to be one of the enumerated profiles

INSERT INTO USERGROUPS SELECT * FROM USERGROUPS_TMP;
DROP TABLE USERGROUPS_TMP;


-- Convert Profile column to the profile enumeration ordinal

ALTER TABLE metadata DROP CONSTRAINT IF EXISTS metadata_owner_fkey;
ALTER TABLE metadatastatus DROP CONSTRAINT IF EXISTS metadatastatus_userid_fkey;
ALTER TABLE useraddress DROP CONSTRAINT IF EXISTS useraddress_userid_fkey;
-- GEOCAT
ALTER TABLE usergroups DROP CONSTRAINT IF EXISTS usergroups_userid_fkey ;
-- END GEOCAT
ALTER TABLE email DROP CONSTRAINT IF EXISTS email_user_id_fkey;
ALTER TABLE groups DROP CONSTRAINT IF EXISTS groups_referrer_fkey;
TRUNCATE TABLE Users;

ALTER TABLE Users DROP COLUMN profile;
ALTER TABLE Users ADD COLUMN profile integer NOT NULL;

ALTER TABLE Users DROP COLUMN address;
ALTER TABLE Users DROP COLUMN city;
ALTER TABLE Users DROP COLUMN state;
ALTER TABLE Users DROP COLUMN zip;
ALTER TABLE Users DROP COLUMN country;
ALTER TABLE Users DROP COLUMN email;
ALTER TABLE Users DROP COLUMN streetnumber;
ALTER TABLE Users DROP COLUMN streetname;
ALTER TABLE Users DROP COLUMN postbox;
ALTER TABLE Users DROP COLUMN positionname;
ALTER TABLE Users DROP COLUMN onlineresource;
ALTER TABLE Users DROP COLUMN onlinename;
ALTER TABLE Users DROP COLUMN onlinedescription;
ALTER TABLE Users DROP COLUMN hoursofservice;
ALTER TABLE Users DROP COLUMN contactinstructions;
ALTER TABLE Users DROP COLUMN publicaccess;
ALTER TABLE Users DROP COLUMN orgacronym;
ALTER TABLE Users DROP COLUMN directnumber;
ALTER TABLE Users DROP COLUMN mobile;
ALTER TABLE Users DROP COLUMN phone;
ALTER TABLE Users DROP COLUMN facsimile;
ALTER TABLE Users DROP COLUMN email1;
ALTER TABLE Users DROP COLUMN phone1;
ALTER TABLE Users DROP COLUMN facsimile1;
ALTER TABLE Users DROP COLUMN email2;
ALTER TABLE Users DROP COLUMN phone2;
ALTER TABLE Users DROP COLUMN facsimile2;
ALTER TABLE Users DROP COLUMN parentinfo;
ALTER TABLE Users DROP COLUMN validated;

-- Convert Profile column to the profile enumeration ordinal
-- GEOCAT
INSERT INTO USERS(id, username, password, surname, name, profile, organisation, kind, security, authtype) SELECT id, username, password, surname, name, profile, organisation, kind, security, authtype FROM USERS_TMP;
-- END GEOCAT
DROP TABLE USERS_TMP;


ALTER TABLE metadata ADD CONSTRAINT metadata_owner_fkey FOREIGN KEY (owner)
      REFERENCES users (id);
ALTER TABLE metadatastatus ADD CONSTRAINT metadatastatus_userid_fkey FOREIGN KEY (userid)
      REFERENCES users (id);
ALTER TABLE useraddress ADD CONSTRAINT useraddress_userid_fkey FOREIGN KEY (userid)
      REFERENCES users (id);
ALTER TABLE email ADD CONSTRAINT email_user_id_fkey FOREIGN KEY (user_id)
      REFERENCES users (id);
ALTER TABLE groups ADD CONSTRAINT groups_referrer_fkey FOREIGN KEY (referrer)
      REFERENCES users (id);
ALTER TABLE usergroups DROP CONSTRAINT IF EXISTS usergroups_userid_fkey ;

-- ----  Change notifier actions column to map to the MetadataNotificationAction enumeration

TRUNCATE TABLE MetadataNotifications;

-- ----  Change notifier actions column to map to the MetadataNotificationAction enumeration

INSERT INTO MetadataNotifications SELECT * FROM MetadataNotifications_Tmp;
DROP TABLE MetadataNotifications_Tmp;

-- ----  Change params querytype column to map to the LuceneQueryParamType enumeration

-- GEOCAT
-- END GEOCAT
