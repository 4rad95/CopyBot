FROM ubuntu:latest
LABEL authors="radomir"

ENTRYPOINT ["top", "-b"]
# Use a base image with Java installed
FROM openjdk:22

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/*.jar app.jar

# Specify the command to run your application
#CMD ["java", "-jar", "app.jar"]
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]