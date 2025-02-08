# 1. OpenJDK 이미지 사용
FROM openjdk:17-jdk-slim AS build

# 2. Gradle 설치
RUN apt-get update && apt-get install -y wget unzip \
    && wget https://services.gradle.org/distributions/gradle-7.6-bin.zip \
    && unzip gradle-7.6-bin.zip -d /opt \
    && ln -s /opt/gradle-7.6/bin/gradle /usr/bin/gradle

# 3. 작업 디렉터리 생성
WORKDIR /app

# 4. Gradle wrapper 파일 복사
COPY gradlew gradlew
COPY gradle gradle
RUN chmod +x gradlew

# 5. 프로젝트의 소스 코드 복사
COPY . .

# 6. Gradle 빌드 (JAR 파일 생성)
RUN ./gradlew build --no-daemon

# 7. 실제 실행할 JAR 파일을 가져와서 실행
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드한 JAR 파일을 복사
COPY build/libs/Individualproject-0.0.1-SNAPSHOT.jar app.jar

# 8. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
