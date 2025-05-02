# setup.sh - Unix/Linux/macOS setup script
#!/bin/bash

# Exit on error
set -e

# Print welcome message
echo "====================================================="
echo "  Neighbor Project - Setup Script"
echo "====================================================="
echo

# Check prerequisites
echo "Checking prerequisites..."

# Check Java
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo "❌ Error: Java 17 or higher is required, found version $JAVA_VERSION"
        echo "Please install Java 17+ and try again."
        exit 1
    else
        echo "✅ Java $JAVA_VERSION detected"
    fi
else
    echo "❌ Error: Java not found"
    echo "Please install Java 17+ and try again."
    exit 1
fi


# Maven or Gradle build
if [ -f "pom.xml" ]; then
    echo "Maven project detected"

    # Give execution permission to mvnw
    if [ -f "./mvnw" ]; then
        chmod +x ./mvnw
        BUILD_CMD="./mvnw"
    else
        BUILD_CMD="mvn"
    fi

    echo "Building project with Maven..."
    $BUILD_CMD clean package

elif [ -f "build.gradle" ]; then
    echo "Gradle project detected"

    # Give execution permission to gradlew
    if [ -f "./gradlew" ]; then
        chmod +x ./gradlew
        BUILD_CMD="./gradlew"
    else
        BUILD_CMD="gradle"
    fi

    echo "Building project with Gradle..."
    $BUILD_CMD clean build
fi

echo
echo "✅ Setup completed successfully!"
echo "You can now run the application using one of the following methods:"
echo
echo "1. Using Java:"
echo "   java -jar target/*.jar"
echo
echo "2. Using Maven:"
echo "   ./mvnw spring-boot:run"
echo
echo "The application will be available at http://localhost:8080"
echo "API endpoint to hit with a POST Request is http://localhost:8080/search/space"
