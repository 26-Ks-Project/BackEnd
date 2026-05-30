package com.example.smart_farm.domain.device.service;

import com.example.smart_farm.global.mqtt.publisher.MqttPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 스프링 서버 -> 라즈베리파이 방향으로 제어명령 전달
 * */

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceControlService {

    // 자네가 만든 지휘소 인터페이스를 주입받네
    private final MqttPublisher mqttPublisher;

    // 워터펌프 작동 명령 메서드
    public void turnOnWaterPump(String deviceId) {
        // 1. 라즈베리 파이가 듣고 있는 토픽 생성
        String topic = "smartfarm/" + deviceId + "/control";

        // 2. 전달할 명령을 JSON 형태의 String으로 생성 (DTO -> JSON 변환)
        String payload = "{\"command\": \"PUMP_ON\", \"duration\": 30}";

        mqttPublisher.sendControlCommand(topic, payload);

        log.info("제어 명령 발송 완료 - 대상: {}, 명령: {}", topic, payload);
    }

    public void turnOnSupplement(String deviceId) {
        String topic = "smartfarm/" + deviceId + "/control";
        // 영양제 공급기 제어를 위한 페이로드 명세서 정의
        String payload = "{\"command\": \"SUPPLEMENT_ON\", \"duration\": 15}";
        mqttPublisher.sendControlCommand(topic, payload);
        log.info("💊 영양제 명령 발송 완료 - 토픽: {}, 페이로드: {}", topic, payload);
    }
}
