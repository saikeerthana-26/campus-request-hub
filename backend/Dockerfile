# ---- build stage ----
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the Spring Boot executable jar (the non-"original" one)
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
