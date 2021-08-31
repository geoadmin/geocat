# geocat.ch 4 migration



* [SQL script for DB migration before starting the app](geocatch-to-gn404-before-startup.sql)
  
* After stratup, sign in, go to http://localhost:8080/geonetwork/doc/api/index.html#/tools/callStep and trigger org.fao.geonet.MetadataResourceDatabaseMigration for updating overview URLs. Check remaining records with (and manually fix them?):

```sql
SELECT uuid, data from metadata where data LIKE '%resources.get%' AND isharvested = 'n';
```

* Map / Update `/datadir/data/resources/map/config-viewer.xml` with [this map configuration file](https://raw.githubusercontent.com/geoadmin/geocat/geocat-4.0.x/web/src/main/webapp/WEB-INF/data/data/resources/map/config-viewer.xml).

* Thesaurus / Copy from prod

* Datadir / Copy metadata data and logo from prod

* [Extent update](../jira-MGEO_SB-650/README.md)


# Misc.


When setting up a new instance:
* Update in admin > Settings > host name
* Update in db URL
```sql
UPDATE metadata SET data = replace(data, 'https://www.geocat.ch/', 'https://geocat-dev.dev.bgdi.ch/') WHERE data LIKE '%https://www.geocat.ch/%';
```
* Reindex catalogue
