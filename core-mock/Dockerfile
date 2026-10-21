# ---- build stage ---------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./gradlew :core-mock:bootJar --no-daemon -x test

# ---- runtime stage -------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/core-mock/build/libs/core-mock.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]

# cap build memory inside docker
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Xmx1g"
