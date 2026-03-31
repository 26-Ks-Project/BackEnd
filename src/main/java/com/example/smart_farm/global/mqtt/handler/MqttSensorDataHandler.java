package com.example.smart_farm.global.mqtt.handler;

import com.example.smart_farm.domain.device.dto.SensorLogDataDto;
import com.example.smart_farm.domain.device.entity.Device;
import com.example.smart_farm.domain.device.entity.SensorLog;
import com.example.smart_farm.domain.device.repository.DeviceRepository;
import com.example.smart_farm.domain.device.repository.SensorLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttSensorDataHandler implements MessageHandler {

    private final ObjectMapper objectMapper;
    private final SensorLogRepository sensorLogRepository;
    private final DeviceRepository deviceRepository; // 연관관계를 위해 Device 조회용

    @Override
    @Transactional
    @ServiceActivator(inputChannel = "mqttInputChannel") // Config의 빈 이름과 매핑
    public void handleMessage(Message<?> message) throws MessagingException {
        // 1. 헤더에서 토픽 추출
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
        String payload = message.getPayload().toString();

        log.debug("MQTT 수신 - 토픽: {}, 페이로드: {}", topic, payload);

        try {
            // 2. 센서 토픽인지 확인 (smartfarm/+/sensor)
            if (topic != null && topic.endsWith("/sensor")) {

                // 3. JSON -> DTO 파싱
                SensorLogDataDto dto = objectMapper.readValue(payload, SensorLogDataDto.class);

                // 4. Device 엔티티 조회 (외래키 매핑용)
                Device device = deviceRepository.findById(dto.getDeviceId())
                        .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 기기입니다. DeviceID: " + dto.getDeviceId()));

                // 5. 이상값(Abnormal) 판별 로직 (임시로 false)
                // TODO: 온도, 습도 등이 기준치를 넘는지 확인
                boolean isAbnormal = checkAbnormalData(dto);

                // 6. Entity 빌드 및 DB 저장
                SensorLog sensorLog = SensorLog.builder()
                        .device(device)
                        .temperature(dto.getTemperature())
                        .humidity(dto.getHumidity())
                        .soilMoisture(dto.getSoilMoisture())
                        .illuminance(dto.getIlluminance())
                        .isAbnormal(isAbnormal)
                        .build();

                sensorLogRepository.save(sensorLog);
                log.info("센서 데이터 DB 저장 완료: DeviceID={}", dto.getDeviceId());
            }
            // AI 결과 토픽 처리 로직을 추가하려면 여기에 else if (topic.endsWith("/ai")) 를 쓰면 되네.

        } catch (Exception e) {
            log.error("MQTT 센서 데이터 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    //이상값 판별 메서드
    private boolean checkAbnormalData(SensorLogDataDto dto) {
        // 예: 온도가 40도를 넘거나 습도가 20% 미만이면 true 반환
        return false;
    }
}