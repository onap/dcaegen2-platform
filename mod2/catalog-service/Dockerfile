FROM onap/integration-java11:7.1.0

ARG PROJECT_BUILD_DIR_NAME
ARG FINAL_JAR

WORKDIR /usr/app
VOLUME /tmp

COPY ${PROJECT_BUILD_DIR_NAME}/${FINAL_JAR} .

EXPOSE 8080
ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "mod-catalog-service.jar"]
