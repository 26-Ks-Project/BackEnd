package com.example.smart_farm.domain.device.service;

import com.example.smart_farm.global.mqtt.publisher.MqttPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeviceControlServiceTest {

    @Mock
    private MqttPublisher mqttPublisher;

    @InjectMocks
    private DeviceControlService deviceControlService;

    @Test
    @DisplayName("turnOnWaterPump 호출 시 올바른 토픽과 페이로드로 MQTT 메시지를 전송한다")
    void turnOnWaterPump_success() {
        // given
        String deviceId = "device-123";
        String expectedTopic = "smartfarm/device-123/control";
        String expectedPayload = "{\"command\": \"PUMP_ON\", \"duration\": 30}";

        // when
        deviceControlService.turnOnWaterPump(deviceId);

        // then
        verify(mqttPublisher).sendControlCommand(expectedTopic, expectedPayload);
    }

    @Test
    @DisplayName("turnOnSupplement 호출 시 올바른 토픽과 페이로드로 MQTT 메시지를 전송한다")
    void turnOnSupplement_success() {
        // given
        String deviceId = "device-456";
        String expectedTopic = "smartfarm/device-456/control";
        String expectedPayload = "{\"command\": \"SUPPLEMENT_ON\", \"duration\": 15}";

        // when
        deviceControlService.turnOnSupplement(deviceId);

        // then
        verify(mqttPublisher).sendControlCommand(expectedTopic, expectedPayload);
    }
}
