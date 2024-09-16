# 도커 이미지용 설정 파일

# OpenJDK 17 JDK 슬림 이미지를 사용하겠음
FROM openjdk:17-jdk-slim

# 애플리케이션을 실행할 작업 디렉토리 /app 을 설정하겠음
WORKDIR /app

# jar 파일을 컨테이너에 복사하겠음
COPY /build/libs/core-0.0.1-SNAPSHOT.jar app.jar

# 환경변수 세팅
ENV SECRET_KEY="localmysecretkeylocalmysecretkeylocalmysecretkeylocalmysecretkeylocalmysecretkey"


# 컨테이너의 포트 8082를 노출하겠음
EXPOSE 8082

# java -jar app.jar 명령어로 애플리케이션을 실행하겠음
ENTRYPOINT ["java", "-jar", "app.jar"]
