# --- STAGE 1: Build Stage (The "Kitchen") ---
# This stage uses a full JDK and Maven to compile your code into a JAR.
# It is temporary and will be deleted after the image is built.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY src ./src

# Build the "Fat JAR" and skip tests for a faster build
RUN mvn clean package -DskipTests

# --- STAGE 2: Run Stage (The "Final Product") ---
# This is the lean image that actually runs on your EC2.
# It contains only the JRE and your compiled JAR.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the JAR from the build stage
# Note: Adjust the *.jar pattern if you have multiple jars in target/
COPY --from=build /app/target/*.jar app.jar

# Create the internal workspace for logs
RUN mkdir -p /app/gamelogs

# Set the environment variable used by your GameLogger
ENV GAME_LOG_DIR=/app/gamelogs

# Optimized JVM settings for Docker/Cloud environments
# We use 'CMD' instead of 'ENTRYPOINT' here so you can easily override
# or add flags (like --mode=botvsbot) when you run the container.
CMD ["java", "-XX:+UseContainerSupport", "-Xmx1g", "-jar", "app.jar"]
