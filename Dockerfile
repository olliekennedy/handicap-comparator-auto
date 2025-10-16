# Dockerfile
# Multi-stage for smaller runtime image
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew --no-daemon installDist

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/install/handicap-comparator-auto/ /app/
ENV PORT=8080
EXPOSE 8080
CMD ["./bin/handicap-comparator-auto"]