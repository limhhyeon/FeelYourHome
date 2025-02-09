FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/Individualproject-0.0.1-SNAPSHOT.jar /app/Individualproject-0.0.1-SNAPSHOT.jar

# 환경변수 설정
ENV JASYPT_SECRET_KEY=${JASYPT_SECRET_KEY}

# 실행 명령어
CMD ["java", "-jar", "/app/Individualproject-0.0.1-SNAPSHOT.jar"]

# 컨테이너가 8080 포트를 리스닝하도록 설정
EXPOSE 8080
