FROM nexus3.onap.org:10001/onap/integration-java11:latest

ARG PROJECT_BUILD_DIR_NAME
ARG FINAL_JAR

WORKDIR /usr/app
VOLUME /tmp

COPY ${PROJECT_BUILD_DIR_NAME}/${FINAL_JAR} .

EXPOSE 8080
ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "mod-catalog-service.jar"]
