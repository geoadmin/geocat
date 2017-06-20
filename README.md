# Build Health

[![Build Status](https://travis-ci.org/geonetwork/core-geonetwork.svg?branch=develop)](https://travis-ci.org/geonetwork/core-geonetwork)

# Geocat
## Build & run
### Docker

A composition `docker-compose.yml` has been added to the project. It contains 2 services:
- database
- geonetwork

`database` starts a container with a postgresql/postgis 9.4 and create an empty base geonetwork with user geonetwork/geonetwork.

`geonetwork` starts a tomcat and map the webapps folder of the tomcat to your sources `web/target/geonetwork` folder. Schemas and ui files are also mapped to your sources to have realtime developpment.

Start your project:
```
git clone --recursive git@github.com:geoadmin/geocat.git
cd geocat
mvn clean install -DskipTests -Ddb.username=geonetwork -Ddb.name=geonetwork -Ddb.type=postgres -Ddb.host=database -Ddb.password=geonetwork
docker-compose up
```

When you want to rebuild your project, you must clean the web/target folder as root, because tomcat created it as root.
Also, you may want to clean some files created by root in your schemas (like schematron-rules files) with a git clean -f (check you have no unwatched file to kepp before cleaning your repository).

A script `instal_merge.sh` can be used to rebuild the project (it also does the target folded cleaning).

Application will be available on localhost:8190/geonetwork

### Jetty

You can still want to work with jetty. But you can still use the database container from docker.
Run your jetty with database configuration postgres mapped to the 55432 port (comes from coker postgres image).

Start your project:
```
mvn clean install -DskipTests
docker-compose up database
mvn jetty:run -Penv-dev -Ddb.username=geonetwork -Ddb.name=geonetwork -Ddb.config.file=../config-db/postgres.xml -Ddb.password=geonetwork -Ddb.port=55432
```

Application will be available on localhost:8080/geonetwork

When you work with jetty, no mapping is done for schemas. Resource copying is done on jetty startup. If you want to update your schemas without starting jetty you can use those commands:
```
cp -R -f schemas/iso19139/src/main/plugin/iso19139/ web/src/main/webapp/WEB-INF/data/config/schema_plugins/
cp -R -f schemas/iso19139.che/src/main/plugin/iso19139.che/ web/src/main/webapp/WEB-INF/data/config/schema_plugins/
cp -R -f schemas/dublin-core/src/main/plugin/dublin-core/ web/src/main/webapp/WEB-INF/data/config/schema_plugins/
```

## geocat configuration

### Database
First time tomcat will start over the empty database, it will fill it with all geonetwork database structure and datas.

### ISO10139.CHE
Go to admin page (admin/admin) and add SIO19139.CHE templates and schemas. It is a fresh new set of records imported from geocat.ch PROD server.

# Features

* Immediate search access to local and distributed geospatial catalogues
* Up- and downloading of data, graphics, documents, pdf files and any other content type
* An interactive Web Map Viewer to combine Web Map Services from distributed servers around the world
* Online editing of metadata with a powerful template system
* Scheduled harvesting and synchronization of metadata between distributed catalogs
* Support for OGC-CSW 2.0.2 ISO Profile, OAI-PMH, SRU protocols
* Fine-grained access control with group and user management
* Multi-lingual user interface

# Documentation

User documentation is in the docs submodule in the current repository and is compiled into html pages during a release for publishing on
a website.

Developer documentation is also in the docs submodule but is being migrated out of that project into the Readme files in each module
in the project.  General documentation for the project as a whole is in this Readme and module specific documentation can be found in
each module (assuming there is module specific documentation required).

# Software Development

Instructions for setting up a development environment/building Geonetwork/compiling user documentation/making a release see:
[Software Development Documentation](/software_development/)

# Testing

With regards to testing Geonetwork is a standard Java project and primarily depends on JUnit for testing.  However there is a very important
issue to consider when writing JUnit tests in Geonetwork and that is the separation between unit tests and integration tests

* *Unit Tests* - In Geonetwork unit tests should be very very quick to execute and not start up any subsystems of the application in order to keep
    the execution time of the unit tests very short.  Integration tests do not require super classes and any assistance methods can be static
    imports, for example statically importing org.junit.Assert or org.junit.Assume or org.fao.geonet.Assert.
* *Integration Tests* - Integration Test typically start much or all of Geonetwork as part of the test and will take longer to run than
    a unit test.  However, even though the tests take longer they should still be implemented in such a way to be as efficient as possible.
    Starting Geonetwork in a way that isolates each integration test from each other integration test is non-trivial.  Because of this
    there are `abstract` super classes to assist with this.  Many modules have module specific Abstract classes.  For example at the time
    that this is being written `domain`, `core`, `harvesters` and `services` modules all have module specific super classes that need to
    be used.  (`harvesting` has 2 superclasses depending on what is to be tested.)
    The easiest way to learn how to implement an integration test is to search for other integration tests in the same module as the class
    you want to test.  The following list provides a few tips:
    * *IMPORTANT*: All Integrations tests *must* end in IntegrationTest.  The build system assumes all tests ending in IntegrationTest is
        an integration test and runs them in a build phase after unit tests.  All other tests are assumed to be unit tests.
    * Prefer unit tests over Integration Tests because they are faster.
    * Search the current module for IntegrationTest to find tests to model your integration test against
    * This you might want integration tests for are:
        * Services: If the service already exists and you quick need to write a test to debug/fix its behaviour.
                    If you are writing a new service it is better to use Mockito to mock the dependencies of the service so the test is
                    a unit test.
        * Harvesters
        * A behaviour that crosses much of the full system

*org.fao.geonet.utils.GeonetHttpRequestFactory*: When making Http requests you should use org.fao.geonet.utils.GeonetHttpRequestFactory instead
    of directly using HttpClient.  This is because there are mock instances of org.fao.geonet.utils.GeonetHttpRequestFactory that can
    be used to mock responses when performing tests.
