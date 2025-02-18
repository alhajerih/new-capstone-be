FROM amazoncorretto:22
LABEL version="1.0"
EXPOSE 8080:8080
WORKDIR /app
COPY target/Shares-0.0.1-SNAPSHOT.jar /app/Shares-0.0.1-SNAPSHOT.jar
# Set environment variable for upload directory (but don't create it)
ENV file.upload-dir=/app/UPLOAD_DIR
ENTRYPOINT ["java","-jar","Shares-0.0.1-SNAPSHOT.jar"]