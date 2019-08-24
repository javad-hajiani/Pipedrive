FROM maven:3.6.1-jdk-8-alpine AS builder
COPY pom.xml /app
COPY src /app/src
RUN mvn -DskipTests -f /app/pom.xml clean package

FROM openjdk:8u212-jre-alpine3.9
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar /app/demo.jar
EXPOSE 8080
WORKDIR /app
ENTRYPOINT ["java","-jar","/app/demo.jar"]