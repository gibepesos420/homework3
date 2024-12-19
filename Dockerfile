# Use an official OpenJDK runtime as the base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the project files into the container
COPY . /app

# Install SBT
RUN apt-get update && \
    apt-get install -y curl && \
    curl -L -o sbt.deb https://scala.jfrog.io/artifactory/debian/sbt-1.8.2.deb && \
    dpkg -i sbt.deb && \
    apt-get update && apt-get install -y sbt && \
    rm sbt.deb

# Expose the application port
EXPOSE 8080

# Build the project
RUN sbt assembly

# Run the application
CMD ["java", "-jar", "target/scala-3.3.4/weather-app-assembly-0.1.jar"]

