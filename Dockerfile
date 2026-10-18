# ---- build stage: compile and run tests inside the container ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q package

# ---- runtime stage: slim JRE + jar + ONNX models ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/facegate-*.jar app.jar
COPY models ./models
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
