package com.example.smart_farm.global.mqtt.handler;

import com.example.smart_farm.domain.device.dto.SensorLogDataDto;
import com.example.smart_farm.global.mqtt.service.MqttService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttSensorDataHandler implements MessageHandler {

    private final ObjectMapper objectMapper;
    private final MqttService smartFarmMqttService; // 전용 서비스 주입

    @Override
    // @Transactional 제거! 통신 핸들러는 가벼워야 합니다.
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
        String payload = message.getPayload().toString();

        try {
            if (topic != null && topic.endsWith("/sensor")) {
                SensorLogDataDto dto = objectMapper.readValue(payload, SensorLogDataDto.class);
                // 실제 저장은 서비스에서 수행
                smartFarmMqttService.saveSensorData(dto);
            }
        } catch (Exception e) {
            // 여기서 에러를 잡아내면 MQTT 연결이 끊기지 않고 다음 메시지를 기다립니다.
            log.error("MQTT 데이터 처리 실패: {}", e.getMessage());
        }
    }
}