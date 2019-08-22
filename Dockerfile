FROM maven:3.6.1-jdk-8-alpine AS builder
COPY src /app/src
COPY pom.xml /app
RUN mvn -f /app/pom.xml clean package

FROM openjdk:8u212-jre-alpine3.9
ENV USER_DATA_DIR="/opt/"
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar /app/demo.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/demo.jar"]