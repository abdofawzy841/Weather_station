# Use an official Maven image as the base image
FROM maven:3.8.4-openjdk-11 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and download the dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code to the container
COPY src ./src

# Build the shaded JAR
RUN mvn package -DskipTests shade:shade

# Use an OpenJDK image as the base image for the final image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the shaded JAR file from the build stage
COPY --from=build /app/target/central-station-1.0-SNAPSHOT.jar /app/central-station.jar

# Specify the command to run the application
CMD ["java", "-jar", "central-station.jar"]
