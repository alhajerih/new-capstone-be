FROM amazoncorretto:22
LABEL version="1.0"
EXPOSE 8761:8761
WORKDIR /app
COPY target/eureka-server-0.0.1-SNAPSHOT.jar /app/eureka-server-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java","-jar","eureka-server-0.0.1-SNAPSHOT.jar"]