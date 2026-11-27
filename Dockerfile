FROM maven:3.9.9-eclipse-temurin-25 AS build

WORKDIR /workspace

COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY frontend/pom.xml frontend/pom.xml

RUN mvn --batch-mode --no-transfer-progress -pl backend -am dependency:go-offline

COPY backend/src backend/src

RUN mvn --batch-mode --no-transfer-progress -pl backend -am package dependency:copy-dependencies \
    -DincludeScope=runtime \
    -DoutputDirectory=/workspace/backend/target/dependency

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /workspace/backend/target/unimart-backend-1.0.0.jar /app/app.jar
COPY --from=build /workspace/backend/target/dependency /app/dependency

EXPOSE 8080

CMD ["java", "--add-modules", "jdk.httpserver", "-cp", "/app/app.jar:/app/dependency/*", "br.com.unimart.api.ApiServer"]
