# geocat.ch 3.4 migration

## Generate database dump
 
```
pg_dump -Fc geocat | gzip -9 -c > geocat-20172507.gz
```

## Save data directory

TODO


## Publish database dump
 
```
scp -3 geocat-prod-bd:/home/admin/geocat-20172507.gz publicshare.camptocamp.com:/var/www/publicshare/htdocs/
```

## Load database dump as postgis 2 database

* create geocat database with postgis extension


```
CREATE DATABASE geocatch
  WITH OWNER = "www-data"
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;
       
CREATE EXTENSION postgis;
```

* Ungzip dump
* Load dump

```
/usr/share/postgresql/9.5/contrib/postgis-2.2/postgis_restore.pl geocat.sql | sudo -u postgres psql -d geocat
```

## Database migration

* Run migration (see migrate-default.sql)


## Data directory migration


## Metadata records migration
