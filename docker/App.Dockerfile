FROM gradle:jdk23 AS build

WORKDIR /home/gradle/src

COPY --chown=gradle:gradle . .

RUN ./gradlew clean build -x test --no-daemon

FROM openjdk:23-jdk-slim

WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/* /app/spring-boot-application.jar
COPY --from=build /home/gradle/src/src/main/resources/application.yml /app/application.yml
COPY docker/entrypoint.sh /app
RUN chmod +x /app/entrypoint.sh

EXPOSE 8080 7000

ENTRYPOINT ["/app/entrypoint.sh"]
