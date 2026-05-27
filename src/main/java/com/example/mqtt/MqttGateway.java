package com.example.mqtt;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway(defaultRequestChannel = "mqttOutputChannel")
public interface MqttGateway {

    @Gateway
    void sendToMqtt(String payload, @Header(MqttHeaders.TOPIC) String topic);
}
