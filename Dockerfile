# Stage 1: build the Angular app
FROM node:22-alpine AS ui
WORKDIR /ui
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build

# Stage 2: build the Spring Boot jar with the UI bundled as static resources
FROM maven:3.9-eclipse-temurin-21 AS api
WORKDIR /api
COPY backend/pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=ui /ui/dist/frontend/browser ./src/main/resources/static
RUN mvn -q -B -DskipTests package

# Stage 3: small runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=api /api/target/*.jar app.jar
# H2 file lives here; mount a volume to keep data across redeploys
ENV DB_PATH=/data/salary
VOLUME /data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
