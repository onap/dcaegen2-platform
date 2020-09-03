FROM dockercentral.it.att.com:5100/com.att.dcae.controller/dcae-openjdk:8-jre-alpine
WORKDIR /usr/app
VOLUME /tmp

ADD target/portal-be-1.0.0-SNAPSHOT.jar portal-be-1.0.0-SNAPSHOT.jar

EXPOSE 8082

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "portal-be-1.0.0-SNAPSHOT.jar"]