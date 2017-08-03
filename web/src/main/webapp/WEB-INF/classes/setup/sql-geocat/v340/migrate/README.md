# geocat.ch 3.4 migration

Geocat.ch migration consist of 4 main steps:

* Migration of the database
* Migration of the data directory
* Migration of the metadata records
* Migration of thesaurus


## Database migration

Database migration consist of:
* migrate the database from geocat.ch production (~GN3.0.0) to current GeoNetwork develop (3.4.0).
* migrate from PostGIS 1.5 to 2
* update admin layers 
* cleanup unused db tables (eg. related to shared objects)

 
```
todaydate=$(date +%Y%m%d)

# Generate database dump on source server
pg_dump -Fc geocat | gzip -9 -c > geocat-$todaydate.gz


# Publish database dump
scp -3 geocat-prod-bd:/home/admin/geocat-$todaydate.gz publicshare.camptocamp.com:/var/www/publicshare/htdocs/


# create geocat database with postgis extension
psql 

>
CREATE DATABASE geocat
  WITH OWNER = "www-data"
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;
       
CREATE EXTENSION postgis;
\q


# Ungzip dump
gzip -d geocat-$todaydate.gz


# Load database dump as postgis 2 database
/usr/share/postgresql/9.5/contrib/postgis-2.2/postgis_restore.pl geocat.sql | sudo -u postgres psql -d geocat


# Run migration (see migrate-default.sql)
psql -d geocat -f WEB-INF/classes/setup/sql-geocat/v340/migrate/migrate-default.sql 
```

TODO:

* CGP harvester removed for the time being
* geocat UI config set to default.

## Data directory migration

TODO


## Metadata records migration

See https://github.com/geoadmin/geocat/blob/geocat_3.4.x/schemas/iso19139.che/src/main/plugin/iso19139.che/process/migration3_4.xsl


## Thesaurus migration


Copy current thesaurus from ```/srv/tomcat/geocat/private/geocat/config/codelist```
in new installation (or use admin interface).
