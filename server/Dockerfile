####
# This Dockerfile is used in order to build a container that runs the Quarkus application in JVM mode
#
# Before building the container image run:
#
# ./gradlew build
#
# Then, build the image with:
#
# docker build -t quarkus/castles-jvm .
#
# Then run the container using:
#
# docker run -i --rm -p 8092:8092 quarkus/castles-jvm
#
# If you want to include the debug port into your docker image
# you will have to expose the debug port (default 5005) like this :  EXPOSE 8092 5050
#
# Then run the container using :
#
# docker run -i --rm -p 8092:8092 -p 5005:5005 -e JAVA_ENABLE_DEBUG="true" quarkus/castles-jvm
#
###
FROM gradle:8.4-jdk-alpine as build

WORKDIR /server
# Only copy dependency-related files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN gradle clean build --no-daemon -Dquarkus.application.name=server > /dev/null 2>&1 || true
# Build rest
COPY src src
RUN gradle assemble --no-daemon -Dquarkus.application.name=server

FROM registry.access.redhat.com/ubi8/ubi-minimal:8.6

ARG JAVA_PACKAGE=java-11-openjdk-headless
ARG RUN_JAVA_VERSION=1.3.8

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# Install java and the run-java script
# Also set up permissions for user `1001`
RUN microdnf install curl ca-certificates ${JAVA_PACKAGE} \
    && microdnf install tzdata-java \
    && microdnf update \
    && microdnf clean all \
    && mkdir /deployments \
    && chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments \
    && curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o /deployments/run-java.sh \
    && chown 1001 /deployments/run-java.sh \
    && chmod 540 /deployments/run-java.sh \
    && echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/conf/security/java.security

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager -Dquarkus.application.name=Castles"
# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --from=build --chown=1001 /server/build/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=1001 /server/build/quarkus-app/*.jar /deployments/
COPY --from=build --chown=1001 /server/build/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=1001 /server/build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 3000
USER 1001

ENTRYPOINT [ "/deployments/run-java.sh" ]
