# Multi-stage build: compile the WAR with Maven, then run it on a plain
# Tomcat 10.1 image (matches the Jakarta EE 10 / Servlet 6.0 namespace this
# app is built against - do not swap in an older Tomcat 9 image, it uses the
# javax.servlet.* namespace and this app will not deploy on it).

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM tomcat:10.1-jdk17-temurin

# mysql/mysqldump client binaries for the Admin > Backup & Restore feature
# (see DatabaseBackupUtil, which shells out to them via ProcessBuilder).
RUN apt-get update && apt-get install -y --no-install-recommends default-mysql-client \
    && rm -rf /var/lib/apt/lists/*

# Deployed as ROOT.war so the app serves from the domain root (no
# /restaurant-ordering-system path segment) - shorter, cleaner QR code URLs.
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /build/target/restaurant-ordering-system.war /usr/local/tomcat/webapps/ROOT.war

COPY docker-entrypoint.sh /usr/local/tomcat/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/tomcat/bin/docker-entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/usr/local/tomcat/bin/docker-entrypoint.sh"]
