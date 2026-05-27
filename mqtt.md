# MQTT 정리

## 1. MQTT란?

- **경량 메시지 프로토콜** — IoT 디바이스에 최적화
- 배터리/네트워크 제한 환경에서 사용
- Publish/Subscribe 패턴 기반

---

## 2. MQTT Client vs Broker

### 한 줄 요약

- **Client** = 데이터를 보내거나 받는 주체
- **Broker** = 중간에서 메시지를 중계하는 서버

### 역할 비교

|           | Client                    | Broker              |
| --------- | ------------------------- | ------------------- |
| 역할      | 메시지 발행/구독          | 메시지 수신 및 전달 |
| 종류      | Publisher, Subscriber     | EMQX, Mosquitto     |
| 예시      | IoT 디바이스, Spring Boot | 서버                |
| 직접 통신 | ❌                        | ✅                  |

### 동작 흐름

```
[디바이스 - Publisher]
    ↓ "temperature/data" 토픽에 발행
[EMQX - Broker]
    ↓ 해당 토픽 구독자에게 전달
[Spring Boot - Subscriber]
    ↓ 데이터 처리
[DB 저장]
```

---

## 3. 전체 아키텍처

### 기본 구조

```
[IoT 디바이스]         [Spring Boot]
   Publisher    →  Broker  →  Subscriber
                  (EMQX)         ↓
                              [DB 저장]
                              [비즈니스 로직]
```

### 프론트엔드 포함 구조

```
[IoT 디바이스]
      ↓ MQTT
   [Broker]
      ↓ MQTT
[Spring Boot] ──── REST/WebSocket ──── [프론트엔드]
      ↓
     [DB]
```

> Broker는 단순 중계만 하고 로직 없음. 실제 처리는 전부 Spring Boot(백엔드)에서.

---

## 4. QoS 레벨 (메시지 보장)

| 레벨  | 설명                        |
| ----- | --------------------------- |
| QoS 0 | 최대 1회 전송 (유실 가능)   |
| QoS 1 | 최소 1회 보장 (중복 가능)   |
| QoS 2 | 정확히 1회 보장 (가장 안전) |

> IoT 센서 데이터는 보통 **QoS 1** 추천

---

## 5. MQTT Broker 종류

| 브로커           | 추천 상황                        |
| ---------------- | -------------------------------- |
| **Mosquitto**    | 로컬/소규모 테스트               |
| **EMQX**         | 대규모 디바이스, 고가용성 (추천) |
| **AWS IoT Core** | AWS 인프라 사용 중               |
| **HiveMQ**       | 엔터프라이즈                     |

### EMQX Docker 실행

```bash
docker run -d --name emqx \
  -p 1883:1883 \
  -p 8083:8083 \
  -p 8084:8084 \
  -p 8883:8883 \
  -p 18083:18083 \
  emqx/emqx:latest
```

| 포트  | 용도                       |
| ----- | -------------------------- |
| 1883  | MQTT                       |
| 8883  | MQTT over TLS              |
| 18083 | 웹 대시보드 (admin/public) |

---

## 6. Spring Boot + MQTT 연동

### 의존성

```xml
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-mqtt</artifactId>
</dependency>
```

### 설정

```yaml
mqtt:
  broker: tcp://localhost:1883
  client-id: spring-client
  topic: my/topic
```

### 예시 코드

```java
// Broker 연결
@Bean
public MqttPahoClientFactory mqttClientFactory() {
    DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
    MqttConnectOptions options = new MqttConnectOptions();
    options.setServerURIs(new String[]{"tcp://localhost:1883"});
    factory.setConnectionOptions(options);
    return factory;
}

// 메시지 수신 (Subscriber)
@ServiceActivator(inputChannel = "mqttInputChannel")
public void handleMessage(String payload) {
    System.out.println("Received: " + payload);
}

// 메시지 발신 (Publisher)
@Bean
@ServiceActivator(inputChannel = "mqttOutboundChannel")
public MessageHandler mqttOutbound() {
    MqttPahoMessageHandler handler = new MqttPahoMessageHandler("client-id", mqttClientFactory());
    handler.setDefaultTopic("my/topic");
    return handler;
}
```

---

## 7. 토픽 설계 (여러 디바이스)

```
devices/{device-id}/sensor/temperature
devices/{device-id}/sensor/humidity
devices/{device-id}/status
```

### 와일드카드

| 와일드카드 | 설명           |
| ---------- | -------------- |
| `+`        | 한 레벨 대체   |
| `#`        | 하위 전체 대체 |

```
devices/+/sensor/#   → 모든 디바이스의 모든 센서 데이터 수신
```

---

## 8. MQTT vs RabbitMQ

|             | MQTT                      | RabbitMQ                   |
| ----------- | ------------------------- | -------------------------- |
| 프로토콜    | MQTT                      | AMQP                       |
| 주 용도     | IoT, 경량 디바이스        | 서버 간 메시지 큐          |
| Spring 지원 | `spring-integration-mqtt` | `spring-boot-starter-amqp` |
| 설정 난이도 | 보통                      | 쉬움                       |
| 메시지 보장 | QoS 레벨                  | 강력 (ACK, DLQ 등)         |
| 라우팅      | 토픽 기반                 | Exchange/Queue/Routing Key |

> **IoT 디바이스 ↔ 서버** → MQTT  
> **서버 ↔ 서버** 비동기 처리 → RabbitMQ

---

## 9. ESP32란?

### NIC 카드 vs ESP32

|            | NIC 카드        | ESP32                              |
| ---------- | --------------- | ---------------------------------- |
| 역할       | 네트워크 연결만 | 네트워크 + 데이터 처리 + 센서 제어 |
| 두뇌       | ❌              | ✅ (CPU 내장)                      |
| 프로그래밍 | ❌              | ✅                                 |
| 센서 연결  | ❌              | ✅                                 |
| 가격       | 1~10만원        | 5천원                              |

### ESP32 스펙

```
- CPU: 240MHz 듀얼코어
- 와이파이: 내장
- 블루투스: 내장
- GPIO 핀: 30개 이상
- 크기: 손가락 2개 크기
- 가격: 5천원 ~ 1만원
```

### ESP32 활용 — 일반 기기를 IoT화

| 일반 기기   | ESP32 추가 후             |
| ----------- | ------------------------- |
| 일반 전등   | 스마트 전등 (원격 ON/OFF) |
| 일반 선풍기 | 앱으로 제어 가능          |
| 냉장고      | 온도 모니터링             |
| 화분        | 토양 습도 자동 측정       |
| 공장 기계   | 진동/온도 원격 감시       |

### ESP32 코드 예시 (Arduino)

```cpp
#include <WiFi.h>
#include <PubSubClient.h>

// MQTT 연결
client.connect("esp32-device");

// 데이터 발행
float temp = 25.3;
client.publish("devices/esp32-01/temperature", String(temp).c_str());
```

---

## 10. 적용 가능한 디바이스 및 분야

### 하드웨어

| 디바이스            | 용도                              |
| ------------------- | --------------------------------- |
| **라즈베리파이**    | 온습도, 카메라, 센서 허브         |
| **Arduino**         | 간단한 센서 데이터 수집           |
| **ESP8266 / ESP32** | 가장 많이 씀, 와이파이 내장, 저렴 |
| **STM32**           | 산업용                            |
| **스마트폰**        | 모바일 IoT 앱                     |

### 활용 분야

| 분야          | 예시                            |
| ------------- | ------------------------------- |
| **스마트홈**  | 전등, 에어컨, 도어락 제어       |
| **농업**      | 토양 습도, 온도 모니터링        |
| **공장/산업** | 기계 상태, 진동 센서            |
| **의료**      | 환자 심박수, 체온 원격 모니터링 |
| **물류**      | GPS 트래킹, 냉장 온도           |
| **자동차**    | 차량 상태 전송                  |

http://www.steves-internet-guide.com/
https://www.hivemq.com/mqtt/

---

## 11. MQTTX vs EMQX vs Mosquitto

### 한 줄 요약

| 도구 | 역할 |
|------|------|
| **MQTTX** | GUI 클라이언트 (발행/구독 테스트용) |
| **EMQX** | 브로커 서버 (메시지 중계) |
| **Mosquitto** | 경량 브로커 + CLI 클라이언트 |

> 셋은 대체재가 아니라 **함께 쓰는 도구**다.

### 역할별 비교

|  | MQTTX | EMQX | Mosquitto |
|------|--------|-------|------------|
| 유형 | 데스크톱 앱 | 서버 | 서버 + CLI |
| 브로커 기능 | ❌ | ✅ | ✅ |
| 클라이언트 기능 | ✅ (GUI) | ✅ (대시보드 내 WebSocket) | ✅ (`mosquitto_sub/pub`) |
| 웹 대시보드 | ❌ | ✅ (`:18083`) | ❌ |
| 설치 | `brew install --cask mqttx` | `docker run emqx/emqx` | `brew install mosquitto` |
| 대규모/고가용성 | 해당 없음 | ✅ | ❌ (소규모용) |
| 라즈베리파이 탑재 | ❌ | ❌ (무거움) | ✅ (초경량) |

### 실전 조합

```
[MQTTX] ──Pub──→ [EMQX Broker] ──Sub──→ [Spring Boot]
                      ↑
               [Mosquitto CLI]
             (자동화 스크립트 테스트)
```

| 상황 | 쓸 도구 |
|------|--------|
| MQTT 처음 공부, 눈으로 Pub/Sub 확인 | **MQTTX** |
| 빠른 CLI 테스트, 자동화 스크립트 | **mosquitto_sub/pub** |
| Spring Boot 연동 전 브로커 상태 확인 | **EMQX 대시보드** |
| 실제 서비스 운영 브로커 | **EMQX** |
| 라즈베리파이에 직접 브로커 올릴 때 | **Mosquitto** |

### 설치

```bash
# 브로커 — EMQX (Docker)
docker run -d --name emqx \
  -p 1883:1883 -p 18083:18083 \
  emqx/emqx:latest

# GUI 클라이언트 — MQTTX
brew install --cask mqttx

# CLI 클라이언트 — Mosquitto 클라이언트만
brew install mosquitto
```

---

## 12. 규모별 브로커 선택

### 규모 기준

| 규모 | 디바이스 수 | 예시 |
|------|-----------|------|
| **소규모** | 1~100대 | 개인 스마트홈, 취미 프로젝트 |
| **중규모** | 100~10,000대 | 스마트 빌딩, 소규모 공장, 스타트업 서비스 |
| **대규모** | 10,000~수백만 대 | 스마트 시티, 대규모 공장, 글로벌 서비스 |

### 규모별 추천

|  | 소규모 | 중규모 | 대규모 |
|------|--------|--------|--------|
| **Broker** | Mosquitto | EMQX (단일 노드) | EMQX Cluster |
| **Client 테스트** | MQTTX, mosquitto CLI | MQTTX + 스크립트 | 부하 테스트 도구 (XMeter 등) |
| **설치 방식** | `apt install mosquitto` | Docker Compose | Kubernetes |
| **모니터링** | CLI 로그 확인 | EMQX 대시보드 | Prometheus + Grafana |

### 왜 그런가?

```
소규모 — Mosquitto
├─ 바이너리 하나, 메모리 3MB
├─ 라즈베리파이에 올려도 충분
└─ 대시보드 없어도 불편하지 않음

중규모 — EMQX 단일 노드
├─ 디바이스 100대 넘으면 Mosquitto는 불안정
├─ 웹 대시보드로 토픽/클라이언트 관리
├─ Docker Compose로 한 방에 배포
└─ 최대 10만 연결까지 단일 노드로 버팀

대규모 — EMQX Cluster
├─ 노드 여러 대 묶어서 무중단, 수평 확장
├─ Kafka/DB로 메시지 브릿지 연동
├─ RBAC, TLS, 속도 제한 등 엔터프라이즈 기능
└─ Prometheus 메트릭 → Grafana 실시간 대시보드
```

### 한눈에

| 질문 | 답 |
|------|----|
| 집에서 전등 5개 제어 | Mosquitto |
| 공장 기계 500대 모니터링 | EMQX (Docker 단일) |
| 글로벌 IoT 플랫폼, 디바이스 10만+ | EMQX Cluster (K8s) |
| Spring Boot 공부 + IoT 프로토타입 | EMQX (Docker) |
