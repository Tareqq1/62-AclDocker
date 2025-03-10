# Use OpenJDK 25 as the base image (per instructor's requirements)
FROM openjdk:25-ea-4-jdk-oraclelinux9

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file
COPY ./target/mini1.jar /app/mini1.jar

# Ensure the JSON directory structure inside the container matches the expected paths
RUN mkdir -p /app/src/main/java/com/example/data

# Copy JSON data files into the expected directory inside the container
COPY ./src/main/java/com/example/data /app/src/main/java/com/example/data

# Expose the Spring Boot application port
EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-jar", "/app/mini1.jar"]
