FROM maven:3.6.3-jdk-11 AS builder

WORKDIR /workspace

COPY .mvn/ /workspace/.mvn
COPY pom.xml .
RUN mvn -s .mvn/settings.xml dependency:go-offline

COPY src /workspace/src
RUN mvn -s .mvn/settings.xml -e -B clean package -DskipTests

FROM adoptopenjdk/openjdk11:x86_64-alpine-jre11u-nightly
#FROM openjdk:8-jre-alpine

LABEL APP="cmsnesia-storeage"
LABEL DOMAIN="cmsnesia-storeage"

RUN addgroup -S cmsnesia && adduser -S cmsnesia-storeage -G cmsnesia
USER cmsnesia-storeage:cmsnesia

WORKDIR /app

COPY --from=builder /workspace/target/cmsnesia-storeage-*.jar /app/cmsnesia-storeage.jar

ENTRYPOINT ["java", "-jar", "/app/cmsnesia-storeage.jar"]