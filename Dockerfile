# OpenJDK 17 기반 이미지 사용
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 환경변수 설정
ENV JASYPT_SECRET_KEY=${JASYPT_SECRET_KEY}

# 실행 명령어 (로그를 stdout으로 출력)
CMD ["java", "-jar", "app.jar"]
EXPOSE 8080
