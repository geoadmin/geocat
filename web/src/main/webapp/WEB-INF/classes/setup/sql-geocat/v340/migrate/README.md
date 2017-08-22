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

TODO:

* CGP harvester removed for the time being
* geocat UI config set to default.


### Extent subtemplate loading from ESRI Shapefile:

Download data from https://shop.swisstopo.admin.ch/fr/products/landscape/boundaries3D

Zip Shapefiles to be imported in separate ZIP files:

```
zip land.zip swissBOUNDARIES3D_1_3_TLM_LANDESGEBIET.*
zip canton.zip swissBOUNDARIES3D_1_3_TLM_KANTONSGEBIET.*
zip commune.zip swissBOUNDARIES3D_1_3_TLM_HOHEITSGEBIET.*
```

Load ZIP files using the API (see api-load-extent-subtemplate.png):

```

#export CATALOG=http://localhost:8080/geonetwork
export CATALOGUSER=fxp
export CATALOGPASS=aaaaaa

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


curl -X POST -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  -H 'Content-Type: multipart/form-data' -H 'Accept: application/json' \
  -F file=@cantons.zip \
 "$CATALOG/srv/api/0.1/registries/actions/entries/import/spatial?uuidAttribute=KANTONSNUM&uuidPattern=geocatch-subtpl-extent-kantonsgebiet-%7B%7Buuid%7D%7D&descriptionAttribute=NAME&geomProjectionTo=EPSG%3A4326&lenient=true&onlyBoundingBox=false&process=build-extent-subtemplate&schema=iso19139&uuidProcessing=OVERWRITE"


curl -X POST -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  -H 'Content-Type: multipart/form-data' -H 'Accept: application/json' \
  -F file=@hoheitsgebiet.zip \
 "$CATALOG/srv/api/0.1/registries/actions/entries/import/spatial?uuidAttribute=BFS_NUMMER&uuidPattern=geocatch-subtpl-extent-hoheitsgebiet-%7B%7Buuid%7D%7D&descriptionAttribute=NAME&geomProjectionTo=EPSG%3A4326&lenient=true&onlyBoundingBox=false&process=build-extent-subtemplate&schema=iso19139&uuidProcessing=OVERWRITE"

```


## Data directory migration

Copy folder from old version to the new one.

TODO: Cleanup ISO19110 uploaded files (#MGEO_SB-97)


## Metadata records migration

See https://github.com/geoadmin/geocat/blob/geocat_3.4.x/schemas/iso19139.che/src/main/plugin/iso19139.che/process/migration3_4.xsl

Apply the transformation to all metadata records.

1. Search all records to be updated

In a browser:
``` 
http://localhost:8080/geonetwork/srv/eng/q?_schema=iso19139.che&_isTemplate=y%20or%20n
``` 

In command line:
``` 
export CATALOG=http://geocat-dev.dev.bgdi.ch/geonetwork
export CATALOGUSER=admin
export CATALOGPASS=admin


rm -f /tmp/cookie; 
curl -s -c /tmp/cookie -o /dev/null -X POST "$CATALOG/srv/eng/info?type=me"; 
export TOKEN=`grep XSRF-TOKEN /tmp/cookie | cut -f 7`; 
curl -X POST -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  "$CATALOG/srv/eng/q?_schema=iso19139.che&_isTemplate=y%20or%20n&_isHarvested=n&summaryOnly=true"
```

2. Select all records to be updated


``` 
curl -X PUT --header 'Content-Type: application/json' \
  --header 'Accept: application/json' \
  -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  "$CATALOG/srv/api/0.1/selections/metadata"
  
  
curl -X GET \
  --header 'Content-Type: application/json' \
  --header 'Accept: application/json' \
  -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  "$CATALOG/srv/api/0.1/selections/metadata"
```

3. Apply migration process to selection

``` 
curl -X POST --header 'Content-Type: application/json' \
  --header 'Accept: application/json' \
  -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  "$CATALOG/srv/api/0.1/processes/migration3_4"
```

4. Follow progress
``` 
curl -X GET --header 'Accept: application/json' \
  "$CATALOG/srv/api/0.1/processes/reports"
```





## Thesaurus migration


Copy current thesaurus from ```/srv/tomcat/geocat/private/geocat/config/codelist```
in new installation (or use admin interface).
