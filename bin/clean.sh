#!/bin/sh

# resolve links - so that script can be called from any dir
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`pwd`/`dirname "$PRG"`

rm -rf $DATA_DIR
. $PRGDIR/update_datadir.sh

dropdb $DB

case $POSTGIS_INSTALL in
  "POSTGIS 2 create")
    createdb -O www-data $DB
    psql -d $DB -c "CREATE EXTENSION postgis;"
    psql -d $DB -c "CREATE EXTENSION postgis_topology;"
    psql -d $DB -f $SQL_DIR/legacy.sql
    ;;
  "template")
    createdb -O www-data $DB -T template_postgis
    ;;
  "POSTGIS 1 create")
    createdb -O www-data $DB
    createlang plpgsql $DB
    psql -d $DB -f $SQL_DIR/postgis.sql
    psql -d $DB -f $SQL_DIR/spatial_ref_sys.sql
    ;;
esac

