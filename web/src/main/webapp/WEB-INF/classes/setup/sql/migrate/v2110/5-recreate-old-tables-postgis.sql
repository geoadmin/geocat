-- Drop the old tables (that are being migrated to an enum) and create them again with new definition

-- Update UserGroups profiles to be one of the enumerated profiles

TRUNCATE TABLE USERGROUPS;
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

-- Convert Profile column to the profile enumeration ordinal

INSERT INTO USERS SELECT * FROM USERS_TMP;
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


-- ----  Change notifier actions column to map to the MetadataNotificationAction enumeration

TRUNCATE TABLE MetadataNotifications;

-- ----  Change notifier actions column to map to the MetadataNotificationAction enumeration

INSERT INTO MetadataNotifications SELECT * FROM MetadataNotifications_Tmp;
DROP TABLE MetadataNotifications_Tmp;

-- ----  Change params querytype column to map to the LuceneQueryParamType enumeration

TRUNCATE TABLE Params;

-- ----  Change params querytype column to map to the LuceneQueryParamType enumeration

INSERT INTO Params SELECT * FROM Params_TEMP;
DROP TABLE Params_TEMP;

CREATE INDEX ParamsNDX1 ON Params(requestId);
CREATE INDEX ParamsNDX2 ON Params(queryType);
CREATE INDEX ParamsNDX3 ON Params(termField);
CREATE INDEX ParamsNDX4 ON Params(termText);