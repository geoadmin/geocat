# geocat.ch analysis of records content

References:
* https://jira.swisstopo.ch/browse/MGEO_SB-518

The idea is to:
* Check what are the sections used or not in existing record?
* What is the status in subtemplates? If fields are not use, maybe we could simplify drastically editor form (eg. table list of format with only name and version - instead of current advanced editor form)
* What are the main value used per field? eg. on other constraints https://jira.swisstopo.ch/browse/MGEO_SB-517.


## List of indicators

Based on analysis of validation rules and knowledge on existing records, it can be relevant to extract the following indicators:

* Subtemplates
 * Formats 
  * Only name & version ? (2 formats have also specification described)
 * Contacts
  * Usage of organisationAcronym, individualFirstName
 * Extents
  * Using vertical or temporal element ?

* Records
 * Extent / Using vertical element ? = 0
 * Extent / temporal element ? = 69 (non moissonné), 249 (au total)
  
  
* Geobasisdatensatz
 * Collective title
 * che:basicGeodataID
 * che:legislationInformation: not empty, list of values to make it more consistent?


## Strategy for analysis

2 options:
a) SQL queries in db and manual analysis using tools like excel
b) Index catalogue content in Elasticsearch and use Kibana to analyse the content

Option b requires:
* to index all fields needed to all indicator to be indexed properly




## Example of SQL queries

Note: Test made on a local old geocat.ch database (not INT or PROD data). So numbers do not reflect actual situation.

```sql
WITH ns AS (
select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
       ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
       ARRAY['che', 'http://www.geocat.ch/2008/che'],
       ARRAY['xsi', 'http://www.w3.org/2001/XMLSchema-instance'],
       ARRAY['gco', 'http://www.isotc211.org/2005/gco']] AS n
)

SELECT uuid, unnest(xpath('//che:CHE_MD_Metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:collectiveTitle/gco:CharacterString/text()',
 XMLPARSE(DOCUMENT data), n))::text  AS node
FROM metadata, ns
WHERE data LIKE '%%'
```

### Subtemplate / Format with more than name and version

```sql
WITH ns AS (
select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
       ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
       ARRAY['che', 'http://www.geocat.ch/2008/che'],
       ARRAY['xsi', 'http://www.w3.org/2001/XMLSchema-instance'],
       ARRAY['gco', 'http://www.isotc211.org/2005/gco']] AS n
)

SELECT uuid, unnest(xpath('//gmd:MD_Format[count(*) > 2]',
 XMLPARSE(DOCUMENT data), n))::text  AS node
FROM metadata, ns
WHERE isTemplate = 's' AND data LIKE '%gmd:MD_Format%'
```
There is 2:

```xml
"uuid";"node"
"8b1f435f-e897-4f02-bb2c-ec7c1afbc8c4";"<gmd:MD_Format xmlns:gmd=""http://www.isotc211.org/2005/gmd"" xmlns:gco=""http://www.isotc211.org/2005/gco"" xmlns:geonet=""http://www.fao.org/geonetwork"">
  <gmd:name>
    <gco:CharacterString>Administrative units GML application schema</gco:CharacterString>
  </gmd:name>
  <gmd:version>
    <gco:CharacterString>version 4.0, GML, version 3.2.1</gco:CharacterString>
  </gmd:version>
  <gmd:specification>
    <gco:CharacterString>D2.8.I.4 Data Specification on Administrative units – Guidelines</gco:CharacterString>
  </gmd:specification>
</gmd:MD_Format>"
"377f2680-b4b0-4b48-8288-7e57aa76af78";"<gmd:MD_Format xmlns:gmd=""http://www.isotc211.org/2005/gmd"" xmlns:gco=""http://www.isotc211.org/2005/gco"" xmlns:geonet=""http://www.fao.org/geonetwork"">
  <gmd:name>
    <gco:CharacterString>Geographical names GML application schema</gco:CharacterString>
  </gmd:name>
  <gmd:version>
    <gco:CharacterString>version 3.0, GML, version 3.2.1</gco:CharacterString>
  </gmd:version>
  <gmd:specification>
    <gco:CharacterString>D2.8.I.3 Data Specification on Geographical names – Guidelines</gco:CharacterString>
  </gmd:specification>
</gmd:MD_Format>"
```

### Extent

```sql
WITH ns AS (
select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
       ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
       ARRAY['che', 'http://www.geocat.ch/2008/che'],
       ARRAY['xsi', 'http://www.w3.org/2001/XMLSchema-instance'],
       ARRAY['gco', 'http://www.isotc211.org/2005/gco']] AS n
)


-- Number of subtemplates having temporal element = None
SELECT uuid, unnest(xpath('//gmd:EX_Extent[gmd:temporalElement]',
 XMLPARSE(DOCUMENT data), n))::text  AS node
FROM metadata, ns
WHERE isTemplate = 's' AND data LIKE '%gmd:EX_Extent%';


-- Number of subtemplates having vertical element = None
SELECT uuid, unnest(xpath('//gmd:EX_Extent[gmd:verticalElement]',
 XMLPARSE(DOCUMENT data), n))::text  AS node
FROM metadata, ns
WHERE isTemplate = 's' AND data LIKE '%gmd:EX_Extent%'


-- Number of non harvested records having temporal element = 69
SELECT uuid, unnest(xpath('//gmd:EX_Extent[gmd:temporalElement]',
 XMLPARSE(DOCUMENT data), n))::text  AS node
FROM metadata, ns
WHERE isTemplate = 'n' AND isHarvested = 'n' AND data LIKE '%gmd:EX_Extent%';


-- Number of non harvested records having temporal element = 0
SELECT uuid, unnest(xpath('//gmd:EX_Extent[gmd:verticalElement]',
 XMLPARSE(DOCUMENT data), n))::text  AS node
FROM metadata, ns
WHERE isTemplate = 'n' AND isHarvested = 'n' AND data LIKE '%gmd:EX_Extent%'

```

### Contact



```sql
WITH ns AS (
select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
       ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
       ARRAY['che', 'http://www.geocat.ch/2008/che'],
       ARRAY['xsi', 'http://www.w3.org/2001/XMLSchema-instance'],
       ARRAY['gco', 'http://www.isotc211.org/2005/gco']] AS n
)
-- Number of records with CHE_CI_ResponsibleParty using organisationAcronym = 847
-- Number of records with CHE_CI_ResponsibleParty using individualLastName = 740
SELECT uuid, count, over FROM (
SELECT uuid, (xpath('count(//che:CHE_CI_ResponsibleParty[che:individualLastName])',
 XMLPARSE(DOCUMENT data), n))[1]::text::integer AS count, (xpath('count(//che:CHE_CI_ResponsibleParty)',
 XMLPARSE(DOCUMENT data), n))[1]::text::integer AS over
FROM metadata, ns
WHERE isTemplate = 'n') AS c WHERE count > 0;
-- Number of records with CHE_CI_ResponsibleParty using individualLastName or not = 415
-- WHERE isTemplate = 'n') AS c WHERE count < over;



```


### Misc examples

```sql
WITH ns AS (
select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
       ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
       ARRAY['che', 'http://www.geocat.ch/2008/che'],
       ARRAY['xsi', 'http://www.w3.org/2001/XMLSchema-instance'],
       ARRAY['gco', 'http://www.isotc211.org/2005/gco']] AS n
)


-- Number of records with no CRS = 4381
SELECT uuid, count FROM (
SELECT uuid, (xpath('count(//gmd:referenceSystemInfo)',
 XMLPARSE(DOCUMENT data), n))[1]::text::integer AS count
FROM metadata, ns
WHERE isTemplate = 'n') AS c WHERE count = 0;
```



### Value extraction

```sql
WITH ns AS (
select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
       ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
       ARRAY['che', 'http://www.geocat.ch/2008/che'],
       ARRAY['xsi', 'http://www.w3.org/2001/XMLSchema-instance'],
       ARRAY['gco', 'http://www.isotc211.org/2005/gco']] AS n
)


-- All distinct value in otherConstraints
SELECT distinct(unnest(xpath('//gmd:otherConstraints/gco:CharacterString/text()',
 XMLPARSE(DOCUMENT data), n)))::text  AS node
FROM metadata, ns
WHERE isTemplate = 'n'

-- All distinct value in otherConstraints
SELECT distinct(unnest(xpath('//che:legislationInformation//text()',
 XMLPARSE(DOCUMENT data), n)))::text  AS node
FROM metadata, ns
WHERE isTemplate = 'n'

```

This could allow to find similar text
```
-- otherConstraints
-- An Extra ','
"Avant tous travaux, contacter le Service technique et industriel"
"Avant tous travaux contacter le Service technique et industriel"
-- A trailing '.' or not
"Les données livrées sont destinées aux propres besoins de l'utilisateur"
"Les données livrées sont destinées aux propres besoins de l'utilisateur."
-- A letter inversion with a wrong link
"Es gelten die Nutzungsbedingungen für Geodaten des Kantons Basel-Stadt. (http://www.geo.bs.ch/abg)"
"Es gelten die Nutzungsbedingungen für Geodaten des Kantons Basel-Stadt. (http://www.geo.bs.ch/agb)"


-- legislationInformation
"162 LATeC , Art- 72, Procédure ordinaire"
"162 LATeC, Art.72, Procédure ordinaire"
"Loi fédérale sur les fôrets"
"Loi fédérale sur les forêts"
"Loi fédérale sur l’aviation (LA)"
"Loi fédérale sur l’aviation"
"Loi fédérale sur la réduction du bruit émis par les chemins de fer (LBCF)"
"Loi fédérale sur la réduction du bruit émis par les chemins de fer"
"Loi sur la géoinformation (Lgéo)"
"Loi sur la géoinformation (LGéo)"
"Loi sur la protection de l'environnement (LPE)"
"Loi sur la protection de l'environnement, LPE"
"Loi sur la protection de l’environnement (LPE)"


```
