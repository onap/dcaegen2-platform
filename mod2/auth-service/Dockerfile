FROM onap/integration-java11:7.1.0
WORKDIR /usr/app
VOLUME /tmp

ADD target/auth-service-1.0.0-SNAPSHOT.jar auth-service-1.0.0-SNAPSHOT.jar

EXPOSE 8082

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "auth-service-1.0.0-SNAPSHOT.jar"]