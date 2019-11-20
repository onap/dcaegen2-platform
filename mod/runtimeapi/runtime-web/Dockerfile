FROM openjdk:8-jre-alpine
WORKDIR /usr/app
VOLUME /tmp
ADD target/runtime-web-0.0.1-SNAPSHOT.jar runtime-web-0.0.1-SNAPSHOT.jar

EXPOSE 9090

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "runtime-web-0.0.1-SNAPSHOT.jar"]
