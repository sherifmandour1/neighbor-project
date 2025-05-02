FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app

# Copy maven files first for better caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the maven wrapper executable
RUN chmod +x ./mvnw

# Download all required dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src
COPY listings.json .

# Build the application
RUN ./mvnw package -DskipTests

# Runtime image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy built application from builder stage
COPY --from=builder /app/target/*.jar /app/app.jar
COPY --from=builder /app/listings.json /app/listings.json

# Set default environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1g"
ENV SERVER_PORT=8080

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]