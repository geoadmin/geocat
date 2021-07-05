# Extent subtemplate loading from ESRI Shapefile:

## Current status

```sql
SELECT count(*) FROM metadata WHERE isTemplate = 's' AND uuid LIKE 'geocatch-subtpl-extent-hoheitsgebiet%';
```
2301 communes.



## Update

Backup 
```sql
CREATE TABLE extentbackup650 AS SELECT * FROM metadata WHERE isTemplate = 's' AND uuid LIKE 'geocatch-subtpl-extent-hoheitsgebiet%';
```


Download data from http://files.titellus.net/gis/ch/ with data aggregated on id
to have one geometry (multi if needed) per features.


```
wget https://files.titellus.net/geonetwork/geocat.ch/2021.zip
unzip 2021.zip
```

Load ZIP files using the API (see api-load-extent-subtemplate.png) - can be launched while catalogue is indexing records on first start:

```
export CATALOG=http://localhost:8080/geonetwork
export CATALOGUSER=admin
export CATALOGPASS=admin


rm -f /tmp/cookie; 
curl -s -c /tmp/cookie -o /dev/null -X POST "$CATALOG/srv/eng/info?type=me"; 
export TOKEN=`grep XSRF-TOKEN /tmp/cookie | cut -f 7`; 
echo "Token is: $TOKEN"
curl -X POST -H "X-XSRF-TOKEN: $TOKEN" --user $CATALOGUSER:$CATALOGPASS -b /tmp/cookie \
  "$CATALOG/srv/eng/info?type=me"

# MUST return @authenticated = true
TOKEN=1d6c92a4-bf6f-4302-b68e-4fadbdb18046
COOKIE="JSESSIONID=node01fodzdfcfaxgaxpuzzw3ug30e6.node0; XSRF-TOKEN=$TOKEN;"

curl \
  -H "Cookie: $COOKIE" \
  -H "X-XSRF-TOKEN: $TOKEN" \
  -H 'Content-Type: multipart/form-data' \
  -H 'Accept: application/json' \
  -F file=@g1g21_encl.zip \
  -X POST "$CATALOG/srv/api/registries/actions/entries/import/spatial?uuidAttribute=GMDNR&uuidPattern=geocatch-subtpl-extent-hoheitsgebiet-%7B%7Buuid%7D%7D&descriptionAttribute=GMDNAME&geomProjectionTo=EPSG%3A4326&lenient=true&onlyBoundingBox=false&process=build-extent-subtemplate&schema=iso19139&uuidProcessing=OVERWRITE&charset=UTF-8"
  
curl \
  -H "Cookie: $COOKIE" \
  -H "X-XSRF-TOKEN: $TOKEN" \
  -H 'Content-Type: multipart/form-data' \
  -H 'Accept: application/json' \
  -F file=@g1g21_li.zip \
  -X POST "$CATALOG/srv/api/registries/actions/entries/import/spatial?uuidAttribute=GMDNR&uuidPattern=geocatch-subtpl-extent-hoheitsgebiet-%7B%7Buuid%7D%7D&descriptionAttribute=GMDNAME&geomProjectionTo=EPSG%3A4326&lenient=true&onlyBoundingBox=false&process=build-extent-subtemplate&schema=iso19139&uuidProcessing=OVERWRITE&charset=UTF-8"
 
 
curl \
  -H "Cookie: $COOKIE" \
  -H "X-XSRF-TOKEN: $TOKEN" \
  -H 'Content-Type: multipart/form-data' \
  -H 'Accept: application/json' \
  -F file=@g1g21.zip \
  -X POST "$CATALOG/srv/api/registries/actions/entries/import/spatial?uuidAttribute=GMDNR&uuidPattern=geocatch-subtpl-extent-hoheitsgebiet-%7B%7Buuid%7D%7D&descriptionAttribute=GMDNAME&geomProjectionTo=EPSG%3A4326&lenient=true&onlyBoundingBox=false&process=build-extent-subtemplate&schema=iso19139&uuidProcessing=OVERWRITE&charset=UTF-8"
 
# Check in db select count(*) from metadata where uuid like 'geocatch-subtpl-extent-hoheitsgebiet%'
```


## Control query

```sql
-- Extent imported
SELECT * FROM metadata 
    WHERE isTemplate = 's' 
      AND uuid LIKE 'geocatch-subtpl-extent-hoheitsgebiet%' 
      AND changedate LIKE '2021-05-27%';
= 2175+13

-- Extent not imported and used
WITH ns AS (
  select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
  ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
  ARRAY['che', 'http://www.geocat.ch/2008/che'],
  ARRAY['xsi', 'http://www.w3.org/2001/XMLSchema-instance'],
  ARRAY['gco', 'http://www.isotc211.org/2005/gco']] AS n
  )
SELECT uuid, unnest(xpath('//gmd:description/gco:CharacterString/text()',
                          XMLPARSE(DOCUMENT data), n))::text as name, (
  SELECT count(*) FROM metadata WHERE data LIKE concat('%', m.uuid, '%')) as record
FROM metadata m, ns
WHERE isTemplate = 's'
  AND uuid LIKE 'geocatch-subtpl-extent-hoheitsgebiet%'
  AND changedate NOT LIKE '2021-05%';

```

Update GML namespace:
```sql

UPDATE Metadata SET data = replace(data, '"http://www.opengis.net/gml"', '"http://www.opengis.net/gml/3.2"') WHERE data LIKE '%"http://www.opengis.net/gml"%' AND schemaId LIKE 'iso19139%' AND uuid LIKE 'geocatch-subtpl-extent-hoheitsgebiet%' AND changedate LIKE '2021-05-27%';

```

Set all extent subtemplate loaded valid and publish them to all in the database:

```sql
INSERT INTO validation
  SELECT id, 'subtemplate', 1, 0, 0, createdate, true
    FROM metadata WHERE (uuid like 'geocatch-subtpl-extent-hoheitsgebiet-%')
    and id not in (SELECT metadataid FROM validation);
    
-- Set publish to all
INSERT INTO operationallowed
  SELECT 1, id, 0
    FROM metadata WHERE (uuid like 'geocatch-subtpl-extent-hoheitsgebiet-%')
                           and id not in (SELECT metadataid FROM operationallowed);

-- Set edit privileges to SUBTEMPLATES group
INSERT INTO operationallowed
  SELECT (SELECT id FROM groups WHERE name = 'SUBTEMPLATES' ORDER by 1 LIMIT 1),
        id, 2
    FROM metadata WHERE (uuid like 'geocatch-subtpl-extent-hoheitsgebiet-%')
                           and id not in (SELECT metadataid FROM operationallowed);
    

-- Assign ownership to geocat admin
UPDATE metadata SET owner = 1 WHERE (uuid like 'geocatch-subtpl-extent-hoheitsgebiet-%');
```

After the change in the database, trigger a reindex of the catalogue.
