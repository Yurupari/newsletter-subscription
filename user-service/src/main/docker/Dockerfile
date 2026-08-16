FROM eclipse-temurin:25-jdk-alpine

ADD user-service/build/libs/user-service.jar /
ENTRYPOINT ["java","-server","-Djava.security.egd=file:/dev/./urandom","-jar","/user-service.jar"]