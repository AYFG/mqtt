# MQTT_EMQX

Spring Boot + MQTT 실습 프로젝트

## 기술 스택

| 구분 | 기술 |
|------|------|
| Framework | Spring Boot 3.5, Java 17 |
| MQTT | spring-integration-mqtt |
| Broker | EMQX (Docker) / Mosquitto (Docker) |
| DB | H2 (인메모리) |
| 빌드 | Gradle |

## 실행 방법

```bash
./gradlew bootRun
```

Spring Boot 실행 시 Docker Compose가 EMQX와 Mosquitto를 자동으로 띄웁니다.

## 접속 정보

| 서비스 | 주소 | 비고 |
|--------|------|------|
| Spring Boot | http://localhost:9092 | REST API |
| EMQX 대시보드 | http://localhost:18083 | admin / public |
| EMQX MQTT | tcp://localhost:1883 | 기본 브로커 |
| Mosquitto MQTT | tcp://localhost:1884 | EMQX와 포트만 다름 |
| H2 콘솔 | http://localhost:9092/h2-console | |
| Mosquitto CLI 구독 | `mosquitto_sub -h localhost -p 1884 -t sensor/data` | |
| Mosquitto CLI 발행 | `mosquitto_pub -h localhost -p 1884 -t sensor/data -m "hello"` | |

## 포트

| 포트 | 용도 |
|------|------|
| 9092 | Spring Boot |
| 1883 | EMQX MQTT |
| 1884 | Mosquitto MQTT |
| 18083 | EMQX 대시보드 |

## 브로커 전환 방법

`application.yml`에서 broker 주소만 바꾸면 됩니다:

```yaml
mqtt:
  # EMQX
  broker: tcp://localhost:1883

  # 또는 Mosquitto
  # broker: tcp://localhost:1884
```
