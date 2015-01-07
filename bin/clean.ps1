$scriptPath = split-path -parent $MyInvocation.MyCommand.Definition
. "$scriptPath\config.ps1"

if (Test-Path "$DATA_DIR" -PathType Any) {rm -Recurse -Force $DATA_DIR}

. "$scriptPath\update_datadir.ps1"

echo "Dropping Database is $DB"
$env:PGPASSWORD = "postgres"
$env:PGUSER = "postgres"
dropdb $DB

echo "Creating Database is $DB"
switch ($POSTGIS_INSTALL)
	{
		"POSTGIS 2 create"{
			echo "creating using Postgres 2 extensions and SQL dir: $SQL_DIR"
			createdb -O www-data $DB
			psql -d $DB -c "CREATE EXTENSION postgis;"
			psql -d $DB -c "CREATE EXTENSION postgis_topology;"
			psql -d $DB -f $SQL_DIR/legacy.sql
		}
		"template" {
			echo "Creating using template: $postgis_template"
			createdb -O www-data -T $postgis_template $DB
		}
		"POSTGIS 1 create" {
			echo "creating using Postgres 1 sql and SQL dir: $SQL_DIR"
			createdb -O www-data $DB
			createlang plpgsql $DB
			psql -d $DB -f $SQL_DIR/postgis.sql
			psql -d $DB -f $SQL_DIR/spatial_ref_sys.sql
		}
		default { 
			echo "Illegal Configuration: POSTGIS_INSTALL=$POSTGIS_INSTALL"
			exit 1
		}
	}


