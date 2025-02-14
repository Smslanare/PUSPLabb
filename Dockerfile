FROM eclipse-temurin:17.0.13_11-jdk

WORKDIR /src
COPY . /src
RUN ls -al /src && ./mvnw package

FROM eclipse-temurin:17.0.13_11-jdk

RUN mkdir -p /app
COPY --from=0 /src/target/*.jar /app/
COPY --from=0 /src/target/libs /app/libs

WORKDIR /app

CMD ["java", "-jar", "base-journal-system-1.0.1.jar"]