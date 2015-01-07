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

#set -x

. $PRGDIR/config.sh

rm -rf $DATA_DIR/config
rm -rf $DATA_DIR/data/formatter

mkdir -p $DATA_DIR/config/schema_plugins
mkdir -p $DATA_DIR/data

cp -R $WEB_DIR/src/main/webapp/WEB-INF/data/config/codelist $DATA_DIR/config/
cp -R $WEB_DIR/src/main/webapp/WEB-INF/data/data/formatter $DATA_DIR/data/

find schemas -name plugin -type d | while read file; do
   cp -r $file/* $DATA_DIR/config/schema_plugins/
done
