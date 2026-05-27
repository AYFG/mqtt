# mqtt

Spring Boot + MQTT 실습 프로젝트

## 기술 스택

| 구분 | 기술 |
|------|------|
| Framework | Spring Boot 3.5, Java 17 |
| MQTT | spring-integration-mqtt |
| Broker | EMQX (Docker) |
| DB | H2 (인메모리) |
| 빌드 | Gradle |

## 실행 방법

```bash
# 프로젝트 루트에서
./gradlew bootRun
```

Spring Boot 실행 시 Docker Compose가 EMQX를 자동으로 띄웁니다.

## 접속 정보

| 서비스 | 주소 | 비고 |
|--------|------|------|
| Spring Boot | http://localhost:9092 | REST API |
| EMQX 대시보드 | http://localhost:18083 | admin / public |
| MQTT 브로커 | tcp://localhost:1883 | |
| H2 콘솔 | http://localhost:9092/h2-console | |

## 포트

| 포트 | 용도 |
|------|------|
| 9092 | Spring Boot |
| 1883 | MQTT |
| 18083 | EMQX 대시보드 |
