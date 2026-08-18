FROM eclipse-temurin:25-jdk-alpine

ADD subscription-service/build/libs/subscription-service.jar /
ENTRYPOINT ["java","-server","-Djava.security.egd=file:/dev/./urandom","-jar","/subscription-service.jar"]