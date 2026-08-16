FROM eclipse-temurin:25-jdk-alpine

ADD api-gateway/build/libs/api-gateway.jar /
ENTRYPOINT ["java","-server","-Djava.security.egd=file:/dev/./urandom","-jar","/api-gateway.jar"]