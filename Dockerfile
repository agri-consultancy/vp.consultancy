# Stage 1: Build the application
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:resolve

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Create logs directory
RUN mkdir -p /var/agri-consultancy/logs && \
    chmod 777 /var/agri-consultancy/logs

# Copy the JAR from builder stage
COPY --from=builder /build/target/vp-consultancy-0.0.1-SNAPSHOT.jar app.jar

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8085/agri-consultancy-service/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

