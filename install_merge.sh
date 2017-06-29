#sudo rm -rf web/target
mvn clean install -DskipTests -Ddb.username=geonetwork -Ddb.name=geonetwork -Ddb.type=postgres -Ddb.host=database -Ddb.password=geonetwork -Penv-dev
