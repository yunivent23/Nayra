# syntax=docker/dockerfile:1
# Nayra-Back (Spring Boot, Java 21). Contexto de build: raíz del repositorio, porque el backend lee el vocabulario
# compartido del desafío (shared/desafio/vocabulario_v2.json, D-054):
#   docker build -f Nayra-Back/Dockerfile -t nayra-back .
# Sin secretos en la imagen (D-017): credenciales, clave JWT y token de servicio llegan por variables de entorno.

FROM eclipse-temurin:21-jdk-noble@sha256:d0aa6704a67ac080591815bb7734dbe56171dd7a8e9b767e9989b95dd37b3085 AS build
WORKDIR /src
COPY Nayra-Back/mvnw Nayra-Back/pom.xml ./
COPY Nayra-Back/.mvn .mvn
RUN --mount=type=cache,target=/root/.m2 sh ./mvnw -B -q dependency:go-offline
COPY Nayra-Back/src src
# Las pruebas no corren aquí: las de persistencia necesitan PostgreSQL (NAYRA_TEST_DB_URL) y se ejecutan aparte.
RUN --mount=type=cache,target=/root/.m2 sh ./mvnw -B -q -DskipTests package \
    && cp target/Nayra-Backend-*.jar /src/app.jar

FROM eclipse-temurin:21-jre-noble@sha256:7edbe8532195c8222735caca437e56780e1fc7db08a4b4b4af77fe188cccc15f
RUN useradd --system --uid 10001 --no-create-home nayra
WORKDIR /app
COPY --from=build /src/app.jar /app/app.jar
COPY shared/desafio/vocabulario_v2.json /app/shared/desafio/vocabulario_v2.json
ENV SPRING_PROFILES_ACTIVE=prototipo \
    NAYRA_VOCABULARIO=/app/shared/desafio/vocabulario_v2.json \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
USER nayra
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
