FROM nexus3.onap.org:10001/onap/integration-java11:latest
WORKDIR /usr/app
VOLUME /tmp

ARG PROJECT_BUILD_DIR_NAME
ARG FINAL_JAR

EXPOSE 8082

COPY ${PROJECT_BUILD_DIR_NAME}/${FINAL_JAR} .

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "mod-auth-service.jar"]
