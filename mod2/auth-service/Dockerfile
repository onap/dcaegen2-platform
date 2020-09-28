FROM onap/integration-java11:7.1.0
WORKDIR /usr/app
VOLUME /tmp

ARG PROJECT_BUILD_DIR_NAME
ARG FINAL_JAR

EXPOSE 8082

COPY ${PROJECT_BUILD_DIR_NAME}/${FINAL_JAR} .

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "auth-service-1.0.0-SNAPSHOT.jar"]