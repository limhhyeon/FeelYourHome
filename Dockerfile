
FROM gradle:7.6-jdk17

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 파일을 컨테이너에 복사
COPY . /app

# Gradle 빌드 실행 (JAR 파일 생성)
RUN gradle build --no-daemon

# 환경변수 설정
ENV JASYPT_SECRET_KEY=${JASYPT_SECRET_KEY}

# 실행 명령어
CMD ["java", "-jar", "/app/build/libs/Individualproject-0.0.1-SNAPSHOT.jar"]

# 컨테이너가 8080 포트를 리스닝하도록 설정
EXPOSE 8080

