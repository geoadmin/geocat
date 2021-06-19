FROM tomcat:8.5.85-jre8

ADD ./server.xml /usr/local/tomcat/conf/

ADD . /

LABEL "io.upkick.warn_only"="true"

# Enable Gzip compression on tomcat
RUN cat /enable_gzip.augtool | augtool --noload --noautoload --echo

RUN cd /usr/local/tomcat/webapps && \
  unzip -d geonetwork geonetwork.war && \
  rm geonetwork.war

ENTRYPOINT ["/docker-entrypoint.sh", "catalina.sh", "run"]
