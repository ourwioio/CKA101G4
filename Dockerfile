# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 先複製 pom，讓 Docker 可以快取 Maven 依賴
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 再複製程式碼並打包可執行 WAR
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

ENV TZ=Asia/Taipei
ENV JAVA_OPTS=""

RUN groupadd --system spring     && useradd --system --gid spring spring

COPY --from=build /app/target/*.war /app/app.war

RUN chown spring:spring /app/app.war
USER spring

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.war"]
