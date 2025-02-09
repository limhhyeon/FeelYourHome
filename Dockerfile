# OpenJDK 17 기반 이미지 사용
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar /app/

# 환경변수 설정
ENV JASYPT_SECRET_KEY=${JASYPT_SECRET_KEY}

# 실행 명령어
CMD ["nohup", "java", "-jar", "app.jar", ">", "app.log", "2>&1", "&"]
EXPOSE 8080