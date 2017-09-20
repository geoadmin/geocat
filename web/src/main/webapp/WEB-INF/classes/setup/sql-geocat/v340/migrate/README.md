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
  WITH OWNER = "geonetwork"
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;

\c geocat
       
CREATE EXTENSION postgis;

\q


# Ungzip dump
gzip -d geocat-$todaydate.gz


# Load database dump as postgis 2 database
/usr/share/postgresql/9.5/contrib/postgis-2.2/postgis_restore.pl geocat-$todaydate.sql | sudo -u postgres psql -d geocat


# Run migration (see migrate-default.sql)
wget https://raw.githubusercontent.com/geoadmin/geocat/geocat_3.4.x/web/src/main/webapp/WEB-INF/classes/setup/sql-geocat/v340/migrate/migrate-default.sql
psql -d geocat -f migrate-default.sql 
```
Run geonetwork.
If DB init issues, then update privileges (to geonetwork)
```
for tbl in `psql -qAt -c "select tablename from pg_tables where schemaname = 'public';" geocat` ; do  psql -c "alter table \"$tbl\" owner to geonetwork" geocat ; done

for tbl in `psql -qAt -c "select sequence_name from information_schema.sequences where sequence_schema = 'public';" geocat` ; do  psql -c "alter table \"$tbl\" owner to geonetwork" geocat ; done
```

TODO:

* CGP harvester removed for the time being
* geocat UI config set to default.
* drop sequences


### Extent subtemplate loading from ESRI Shapefile:

Download data from http://files.titellus.net/gis/ch/ with data aggregated on id
to have one geometry (multi if needed) per features.
(origin from https://shop.swisstopo.admin.ch/fr/products/landscape/boundaries3D)


```
wget http://files.titellus.net/gis/ch/land.zip
wget http://files.titellus.net/gis/ch/cantons.zip
wget http://files.titellus.net/gis/ch/hoheitsgebiet.zip
```

Load ZIP files using the API (see api-load-extent-subtemplate.png):

```
#export CATALOG=http://localhost:8080/geonetwork
export CATALOG=http://geocat-dev.dev.bgdi.ch/geonetwork
export CATALOGUSER=admin
export CATALOGPASS=admin


rm -f /tmp/cookie; 
curl -s -c /tmp/cookie -o /dev/null -X POST "$CATALOG/srv/eng/info?type=me"; 
export TOKEN=`grep XSRF-TOKEN /tmp/cookie | cut -f 7`; 
curl -X POST -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  "$CATALOG/srv/eng/info?type=me"

# MUST return authenticated = true



curl -X POST -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  -H 'Content-Type: multipart/form-data' -H 'Accept: application/json' \
  -F file=@land.zip \
  "$CATALOG/srv/api/0.1/registries/actions/entries/import/spatial?uuidAttribute=ICC&uuidPattern=geocatch-subtpl-extent-landesgebiet-%7B%7Buuid%7D%7D&descriptionAttribute=NAME&geomProjectionTo=EPSG%3A4326&lenient=true&onlyBoundingBox=false&process=build-extent-subtemplate&schema=iso19139&uuidProcessing=OVERWRITE"

# Check in db select count(*) from metadata where uuid like 'geocatch-subtpl-extent-land%'
# = 4

curl -X POST -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  -H 'Content-Type: multipart/form-data' -H 'Accept: application/json' \
  -F file=@cantons.zip \
 "$CATALOG/srv/api/0.1/registries/actions/entries/import/spatial?uuidAttribute=KANTONSNUM&uuidPattern=geocatch-subtpl-extent-kantonsgebiet-%7B%7Buuid%7D%7D&descriptionAttribute=NAME&geomProjectionTo=EPSG%3A4326&lenient=true&onlyBoundingBox=false&process=build-extent-subtemplate&schema=iso19139&uuidProcessing=OVERWRITE"

# Check in db select count(*) from metadata where uuid like 'geocatch-subtpl-extent-kanton%'
# = 26

curl -X POST -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  -H 'Content-Type: multipart/form-data' -H 'Accept: application/json' \
  -F file=@hoheitsgebiet.zip \
 "$CATALOG/srv/api/0.1/registries/actions/entries/import/spatial?uuidAttribute=BFS_NUMMER&uuidPattern=geocatch-subtpl-extent-hoheitsgebiet-%7B%7Buuid%7D%7D&descriptionAttribute=NAME&geomProjectionTo=EPSG%3A4326&lenient=true&onlyBoundingBox=false&process=build-extent-subtemplate&schema=iso19139&uuidProcessing=OVERWRITE"
 
# Check in db select count(*) from metadata where uuid like 'geocatch-subtpl-extent-hoheitsgebiet%'
# = 2285

```


Set all extent subtemplate loaded valid in the database:

```

INSERT INTO validation
  SELECT id, 'subtemplate', 1, 0, 0, createdate, true
    FROM metadata WHERE uuid like 'geocatch-subtpl-extent-land-%'
    or  uuid like 'geocatch-subtpl-extent-kanton-%' 
    or  uuid like 'geocatch-subtpl-extent-hoheitsgebiet-%';
    
```


TODO: There is an issue with BatchOpsMetadataReindexer to solve here as the number of subtemplate
in the catalogue is correct only after a reindex.



## Data directory migration

Copy folder from old version to the new one.

TODO: Cleanup ISO19110 uploaded files (#MGEO_SB-97)


## Metadata records migration

See https://github.com/geoadmin/geocat/blob/geocat_3.4.x/schemas/iso19139.che/src/main/plugin/iso19139.che/process/migration3_4.xsl

Apply the transformation to all metadata records.

0. (optinal) Turn off xlink resolution to make this faster.


1. Search all records to be updated

In a browser:
``` 
http://localhost:8080/geonetwork/srv/eng/q?_schema=iso19139.che&_isTemplate=y%20or%20n&_isHarvested=n&bucket=m&summaryOnly=true
``` 

In command line (does not work use browser mode):
``` 
export CATALOG=http://geocat-dev.dev.bgdi.ch/geonetwork
export CATALOGUSER=admin
export CATALOGPASS=admin


rm -f /tmp/cookie; 
curl -s -c /tmp/cookie -o /dev/null -X POST "$CATALOG/srv/eng/info?type=me"; 
export TOKEN=`grep XSRF-TOKEN /tmp/cookie | cut -f 7`; 
curl -X POST -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  "$CATALOG/srv/eng/q?_schema=iso19139.che&_isTemplate=y%20or%20n&_isHarvested=n&summaryOnly=true&bucket=m"
```

2. Select all records to be updated


http://localhost:8080/geonetwork/doc/api/#!/selections/addToSelection_1

Parameters:
* bucket=m


``` 
curl -X PUT --header 'Content-Type: application/json' \
  --header 'Accept: application/json' \
  -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  "$CATALOG/srv/api/0.1/selections/m"
  
  
curl -X GET \
  --header 'Content-Type: application/json' \
  --header 'Accept: application/json' \
  -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  "$CATALOG/srv/api/0.1/selections/m"
```

Should return around 4757 records to update (depending on the db version used).


3. Apply migration process to selection (~1.2hour without indexing)


http://localhost:8080/geonetwork/doc/api/#!/processes/processRecordsUsingXslt_1

Parameters:
* content-type=application/json
* bucket=m
* process=migration3_4
* index=false



``` 
curl -X POST --header 'Content-Type: application/json' \
  --header 'Accept: application/json' \
  -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  "$CATALOG/srv/api/0.1/processes/migration3_4?index=false"
```

4. Follow progress
``` 
curl -X GET --header 'Accept: application/json' \
  "$CATALOG/srv/api/0.1/processes/reports"
```

5. (optional - if you did not turned off xlinks resolution) Reindex the catalogue from the admin page.



## Thesaurus migration


Copy current thesaurus from ```/srv/tomcat/geocat/private/geocat/config/codelist```
in new installation (or use admin interface).



## Indexing errors

```
Error on line 621 of language-default.xsl:
  XTTE0790: A sequence of more than one item is not allowed as the first argument of
  geonet:protocolMimeType() ("https://map.geo.admin.ch/?topi...",
  "https://map.geo.admin.ch/?topi...", ...) 
2017-09-18 09:19:49,769 ERROR [geonetwork.index] - Indexing stylesheet contains errors: A sequence of more than one item is not allowed as the first argument of geonet:protocolMimeType() ("https://map.geo.admin.ch/?topi...", "https://map.geo.admin.ch/?topi...", ...)  
	 Marking the metadata as _indexingError=1 in index
	 
	 
	 
	 
2017-09-18 09:45:07,448 WARN  [geonetwork.index] - Invalid value. Field 'denominator' is not added to the document. Error is: For input string: "25'000"
```
