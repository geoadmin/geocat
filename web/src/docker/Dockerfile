FROM tomcat:8-jre8

# Install aws cli tool for S3 sync of MD
RUN apt update && apt install -y python-pip && pip install awscli && rm -rf /var/lib/apt/lists/*

ADD . /

ENTRYPOINT ["/docker-entrypoint.sh", "catalina.sh", "run"]
