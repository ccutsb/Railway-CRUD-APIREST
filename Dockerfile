FROM eclipse-temurin:21-jdk AS build

COPY . /app
WORKDIR /app

RUN chmod +x mvnw
RUN ./mvnw -q clean package -DskipTests
RUN mv -f target/*.jar app.jar

FROM eclipse-temurin:21-jre

ARG PORT
ENV PORT=${PORT}
ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=build /app/app.jar .

RUN useradd runtime
USER runtime

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
