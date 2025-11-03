FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
# 배치는 보통 내부 호출용, 포트 노출 안해도 됨 (필요하면 아래 추가)
# EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
