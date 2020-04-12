FROM openjdk:8-jdk-alpine

# Add Maintainer Info
LABEL maintainer="aishwary.jh@gmail.com"

#Adding required libraries
RUN apk add curl

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/*.jar

# Add the application's jar to the container
ADD ${JAR_FILE} kafka-consumer-lag-reporter.jar


ADD src/main/resources/config.properties /

ENV CONFIG_FILE_PATH=/config.properties

# Run the jar file 
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/kafka-consumer-lag-reporter.jar"]