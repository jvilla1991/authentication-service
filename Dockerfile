# Use a lightweight base image with Java
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy your jar into the image
COPY target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]