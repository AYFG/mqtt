package com.example.mqtt;

import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mqtt")
public class MqttController {

    private final MqttGateway mqttGateway;

    public MqttController(MqttGateway mqttGateway) {
        this.mqttGateway = mqttGateway;
    }

    // POST /mqtt/publish?topic=test/hello&message=안녕
    @PostMapping("/publish")
    public String publish(@RequestParam String topic,
                          @RequestParam String message) {
        mqttGateway.sendToMqtt(message, topic);
        return "✅ 발행 완료 — topic: " + topic + ", message: " + message;
    }
}
