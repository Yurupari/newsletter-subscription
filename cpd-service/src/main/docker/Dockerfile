FROM eclipse-temurin:25-jdk-alpine

ADD cpd-service/build/libs/cpd-service.jar /
ENTRYPOINT ["java","-server","-Djava.security.egd=file:/dev/./urandom","-jar","/cpd-service.jar"]