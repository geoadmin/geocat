$LOGFILE="file://$WEB_DIR/dev-config/log4j-jeichar.cfg"

$POSTGIS_INSTALL="POSTGIS 2 create"

$LOGS = ".\web\logs
if (Test-Path "$LOGS" -PathType Any) {rm -Recurse -Force $LOGS}