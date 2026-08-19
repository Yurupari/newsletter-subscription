FROM eclipse-temurin:25-jdk-alpine

ADD notification-service/build/libs/notification-service.jar /
ENTRYPOINT ["java","-server","-Djava.security.egd=file:/dev/./urandom","-jar","/notification-service.jar"]