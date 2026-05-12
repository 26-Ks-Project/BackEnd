package com.example.smart_farm.global.mqtt.service;

import com.example.smart_farm.domain.device.dto.SensorLogDataDto;
import com.example.smart_farm.domain.device.entity.Device;
import com.example.smart_farm.domain.device.entity.SensorLog;
import com.example.smart_farm.domain.device.repository.DeviceRepository;
import com.example.smart_farm.domain.device.repository.SensorLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttService {

    private final DeviceRepository deviceRepository;
    private final SensorLogRepository sensorLogRepository;

    @Transactional // 실제 DB 작업이 일어나는 이곳에 선언
    public void saveSensorData(SensorLogDataDto dto) {
        Device device = deviceRepository.findById(dto.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 기기: " + dto.getDeviceId()));

        SensorLog sensorLog = SensorLog.builder()
                .device(device)
                .temperature(dto.getTemperature())
                .humidity(dto.getHumidity())
                .soilMoisture(dto.getSoilMoisture())
                .illuminance(dto.getIlluminance())
                .isAbnormal(false) // 필요한 로직 추가
                .build();

        sensorLogRepository.save(sensorLog);
        log.info("데이터 저장 성공! DeviceID: {}", dto.getDeviceId());
    }
}
