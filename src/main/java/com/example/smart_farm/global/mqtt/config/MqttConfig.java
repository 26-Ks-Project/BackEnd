package com.example.smart_farm.global.mqtt.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

    private static final String BROKER_URL = "tcp://host.docker.internal:1883";
    private static final String CLIENT_ID = "spring-boot-server";

    // 라즈베리 파이로부터 센서 데이터 및 AI 분석 결과를 받을 토픽 [cite: 69, 83]
    private static final String SENSOR_TOPIC = "smartfarm/+/sensor";
    private static final String AI_TOPIC = "smartfarm/+/ai";

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { BROKER_URL });
        options.setCleanSession(true);

        options.setAutomaticReconnect(true); // 연결 끊기면 알아서 다시 붙음
        options.setKeepAliveInterval(60);    // 60초마다 생존 확인
        options.setConnectionTimeout(30);    // 연결 대기 시간 30초

        factory.setConnectionOptions(options);
        return factory;
    }

    // [수신] MQTT Inbound Channel Adapter
    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(CLIENT_ID + "-inbound", mqttClientFactory(), SENSOR_TOPIC, AI_TOPIC);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // [발신] MQTT Outbound Channel Adapter
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(CLIENT_ID + "-outbound", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("smartfarm/control"); // 기본 제어 토픽
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

}
