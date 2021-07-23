# geocat.ch 4 migration



* [SQL script for DB migration before starting the app](geocatch-to-gn404-before-startup.sql)
  
* After stratup, sign in, go to http://localhost:8080/geonetwork/doc/api/index.html#/tools/callStep and trigger org.fao.geonet.MetadataResourceDatabaseMigration for updating overview URLs. Check remaining records with (and manually fix them?):

```sql
SELECT uuid, data from metadata where data LIKE '%resources.get%' AND isharvested = 'n';
```

* Map / Update `/datadir/data/resources/map/config-viewer.xml` with [this map configuration file](https://raw.githubusercontent.com/geoadmin/geocat/geocat-4.0.x/web/src/main/webapp/WEB-INF/data/data/resources/map/config-viewer.xml).


