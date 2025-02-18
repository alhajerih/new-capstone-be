FROM amazoncorretto:22
LABEL version="1.0"
EXPOSE 8765:8765
WORKDIR /app
COPY target/api-gateway-0.0.1-SNAPSHOT.jar /app/api-gateway-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java","-jar","api-gateway-0.0.1-SNAPSHOT.jar"]