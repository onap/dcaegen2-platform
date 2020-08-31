FROM onap/integration-java11:7.1.0
WORKDIR /usr/app
VOLUME /tmp

ADD target/mod-catalog-service.jar mod-catalog-service.jar
EXPOSE 8080
ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "mod-catalog-service.jar"]
