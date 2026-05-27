package com.example.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.default-topic}")
    private String defaultTopic;

    @Value("${mqtt.qos}")
    private int qos;

    // 1. MQTT 클라이언트 팩토리 — 브로커 연결
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{broker});
        options.setCleanSession(true);
        factory.setConnectionOptions(options);
        return factory;
    }

    // 2. Inbound 채널 — 브로커에서 메시지 받을 통로
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // 3. Inbound 어댑터 — 브로커 구독
    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId + "-sub", mqttClientFactory(), defaultTopic);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(qos);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // 4. 메시지 수신 핸들러 — Subscriber
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttMessageHandler() {
        return message -> {
            System.out.println("📨 MQTT 수신");
            message.getHeaders().forEach((key, value) ->
                System.out.println("   Header: " + key + " = " + value));
            System.out.println("   Payload: " + message.getPayload());
        };
    }

    // 5. Outbound 채널 — 브로커로 메시지 보낼 통로
    @Bean
    public MessageChannel mqttOutputChannel() {
        return new DirectChannel();
    }

    // 6. Outbound 핸들러 — Publisher
    @Bean
    @ServiceActivator(inputChannel = "mqttOutputChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(clientId + "-pub", mqttClientFactory());
        handler.setDefaultTopic(defaultTopic);
        return handler;
    }
}
