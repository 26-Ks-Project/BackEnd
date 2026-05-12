package com.example.smart_farm.domain.device.service;

import com.example.smart_farm.domain.device.entity.Device;
import com.example.smart_farm.domain.device.entity.SensorLog;
import com.example.smart_farm.domain.device.repository.DeviceRepository;
import com.example.smart_farm.domain.device.repository.SensorLogRepository;
import com.example.smart_farm.domain.device.dto.ControlCommandDto;
import com.example.smart_farm.domain.device.dto.SensorLogDataDto;
import com.example.smart_farm.global.mqtt.publisher.MqttPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartFarmMqttService {

    private final DeviceRepository deviceRepository; // Device 조회를 위해 반드시 필요
    private final SensorLogRepository sensorLogRepository;
    private final MqttPublisher mqttPublisher;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    @Transactional // DB 저장이 포함되어 있으니 트랜잭션을 걸어주는 게 안전하네
    public void handleMessage(Message<String> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        String payload = message.getPayload();

        try {
            if (topic != null && topic.endsWith("/sensor")) {
                processSensorData(payload);
            } else if (topic != null && topic.endsWith("/ai")) {
                processAiData(payload);
            }
        } catch (Exception e) {
            log.error("MQTT 메시지 처리 중 오류 발생: {}", e.getMessage());
        }
    }

    private void processSensorData(String payload) throws JsonProcessingException {
        SensorLogDataDto dto = objectMapper.readValue(payload, SensorLogDataDto.class);

        // 1. DB에서 Device 객체 조회
        Device device = deviceRepository.findById(dto.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 기기입니다: " + dto.getDeviceId()));

        // 2. 이상값(isAbnormal) 판단 로직
        boolean isAbnormal = dto.getTemperature().compareTo(new BigDecimal("40.0")) > 0 ||
                dto.getSoilMoisture().compareTo(new BigDecimal("10.0")) < 0;

        // 3. DB에 센서 데이터 저장
        SensorLog logEntity = SensorLog.builder()
                .device(device) 
                .temperature(dto.getTemperature())
                .humidity(dto.getHumidity())
                .soilMoisture(dto.getSoilMoisture())
                .illuminance(dto.getIlluminance())
                .isAbnormal(isAbnormal)
                .build();

        sensorLogRepository.save(logEntity);

        // 4. 생육 자동화 로직 판단 (BigDecimal 비교법 사용)
        if (dto.getSoilMoisture().compareTo(new BigDecimal("30.0")) < 0) {
            // 명세서에 맞게 action_type="WATER", trigger_type="AUTO"로 전송
            triggerActuator(dto.getDeviceId(), "WATER", "AUTO");
        }
    }

    private void processAiData(String payload) {
        // AI 해충 탐지 결과 처리 로직 구현
    }

    // 제어 명령을 JSON으로 변환하여 MQTT로 발행
    public void triggerActuator(String deviceId, String actionType, String triggerType) {
        try {
            // ControlCommandDto 역시 변경된 명세에 맞춰 내부 구조를 수정해 주어야 하네.
            ControlCommandDto command = ControlCommandDto.builder()
                    .actionType(actionType)   // 예: WATER, LED [cite: 42]
                    .triggerType(triggerType) // 예: AUTO, MANUAL [cite: 43]
                    .build();

            String jsonPayload = objectMapper.writeValueAsString(command);
            String controlTopic = "smartfarm/" + deviceId + "/control"; // farmId 대신 deviceId 사용

            mqttPublisher.sendControlCommand(controlTopic, jsonPayload);
            log.info("제어 명령 전송 완료: 토픽={}, 페이로드={}", controlTopic, jsonPayload);

            // 주의: 이 명령을 내린 후, DB의 `control_logs` 테이블에도 이력을 INSERT 하는 로직이 추가되면 완벽하네!
        } catch (JsonProcessingException e) {
            log.error("제어 명령 JSON 변환 실패", e);
        }
    }
}