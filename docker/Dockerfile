# Etapa de build: JDK 17 compila e empacota o jar.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q -DskipTests package

# Etapa de execução: apenas o JRE 17, sem usuário root.
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --no-create-home trimly
COPY --from=build /app/target/*.jar app.jar
USER trimly
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
