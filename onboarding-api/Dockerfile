# ---- build stage ---------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :onboarding-api:bootJar --no-daemon -x test

# ---- runtime stage -------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/onboarding-api/build/libs/onboarding-api.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]

# cap build memory inside docker
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Xmx1g"
