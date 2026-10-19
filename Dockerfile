FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw

COPY backend/src src
COPY index.html app.js tierrastyle.css src/main/resources/static/
RUN mkdir -p "src/main/resources/static/Pantalla inicial"
COPY ["Pantalla inicial/index1.html", "Pantalla inicial/styles.css", "Pantalla inicial/script.js", "src/main/resources/static/Pantalla inicial/"]

RUN ./mvnw -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /workspace/target/earthquakes-api-*.jar app.jar

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
