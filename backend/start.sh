#!/bin/bash

# Code Archeologist Spring Boot Startup Script

echo "Starting Code Archeologist Spring Boot Application..."

# Check if Java 17+ is available
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Java version: $java_version"

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Maven not found. Please install Maven to build the project."
    exit 1
fi

# Build the project
echo "Building the project..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "Build successful. Starting the application..."
    mvn spring-boot:run
else
    echo "Build failed. Please check the errors above."
    exit 1
fi
