FROM tomcat:8-jre8

# Install aws cli tool for S3 sync of MD
RUN apt update && \
    apt install -y python-pip augeas-tools augeas-lenses && \
    pip install awscli && \
    rm -rf /var/lib/apt/lists/*

ADD . /

# Enable Gzip compression on tomcat
RUN cat /enable_gzip.augtool | augtool --noload --noautoload --echo

RUN cd /usr/local/tomcat/webapps && \
  unzip -d geonetwork geonetwork.war && \
  rm geonetwork.war

ENTRYPOINT ["/docker-entrypoint.sh", "catalina.sh", "run"]
