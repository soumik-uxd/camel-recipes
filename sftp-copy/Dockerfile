FROM eclipse-temurin:17-jdk-jammy as builder

LABEL version="0.1"
LABEL description="Camel route to copy from one sftp source to another"
LABEL maintainer="Soumik Das -- soumik.uxd@gmail.com"
LABEL source_url=""

WORKDIR app

# Copy maven artifacts
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x ./mvnw
# download the dependency if needed or if the pom file is changed
RUN ./mvnw dependency:go-offline -B

# Copy sources
COPY src src

# Build the package and copy to target path
RUN ./mvnw clean package verify -DskipTests

FROM eclipse-temurin:17-jre-jammy
ARG DEPENDENCY=/app/target
ENV LANG C.UTF-8

# Copy the dependency application file from build stage artifact
COPY --from=builder ${DEPENDENCY}/*.jar .

ENTRYPOINT [ "java", "-jar", "sftp-copy.jar" ]