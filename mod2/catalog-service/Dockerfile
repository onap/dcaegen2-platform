FROM openjdk:8-jre-alpine
WORKDIR /usr/app
VOLUME /tmp

ADD target/catalog-service-0.0.1-SNAPSHOT.jar catalog-service-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "catalog-service-0.0.1-SNAPSHOT.jar"]
