package com.example.smart_farm.domain.device.controller;

import com.example.smart_farm.domain.device.dto.SensorAvgResponse;
import com.example.smart_farm.domain.device.dto.SensorDataResponseDto;
import com.example.smart_farm.domain.device.entity.SensorLog;
import com.example.smart_farm.domain.device.repository.SensorLogRepository;
import com.example.smart_farm.domain.device.service.DeviceControlService;
import com.example.smart_farm.domain.device.service.SensorLogService;
import com.example.smart_farm.global.mqtt.service.MqttService; // 🎯 MqttService 패키지 경로에 맞게 수정 필요
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // 프론트 연동 대비 CORS 추가
public class DeviceController {

    private final SensorLogRepository sensorLogRepository;
    private final SensorLogService sensorLogService;
    private final DeviceControlService deviceControlService; // 🎯 아까 말씀하신 MqttService 주입 추가

    /**
     * 1. 실시간 센서 데이터 조회 (카드 폴링용 - 최신 1건)
     */
    @GetMapping("/{deviceId}/sensors")
    public ResponseEntity<SensorDataResponseDto> getLatestSensorData(@PathVariable String deviceId) {
        SensorLog latestLog = sensorLogRepository.findTopByDeviceIdOrderByCreatedAtDesc(deviceId)
                .orElseThrow(() -> new RuntimeException("해당 디바이스의 데이터가 존재하지 않습니다."));

        SensorDataResponseDto.SensorLogDetailDto detail = convertToDetailDto(latestLog);

        return ResponseEntity.ok(SensorDataResponseDto.builder()
                .deviceId(deviceId)
                .data(List.of(detail))
                .build());
    }

    /**
     * 2. 센서 히스토리 조회 (그래프 초기 로딩용)
     */
    @GetMapping("/{deviceId}/history")
    public ResponseEntity<SensorDataResponseDto> getSensorHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "100") int limit) {

        Pageable pageable = PageRequest.of(0, limit);
        List<SensorLog> logs = sensorLogRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId, pageable);

        List<SensorDataResponseDto.SensorLogDetailDto> dataList = logs.stream()
                .map(this::convertToDetailDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(SensorDataResponseDto.builder()
                .deviceId(deviceId)
                .data(dataList)
                .build());
    }

    /**
     * 3. 특정 기기의 오늘(00:00~23:59) 2시간 단위 센서 평균 데이터 조회
     */
    @GetMapping("/{deviceId}/today-avg")
    public ResponseEntity<List<SensorAvgResponse>> getTodayAverages(@PathVariable String deviceId) {
        List<SensorAvgResponse> data = sensorLogService.getTodayTwoHourAverages(deviceId);
        return ResponseEntity.ok(data);
    }

    /**
     * 4. [추가] 물펌프 제어 API
     */
    @PostMapping("/{deviceId}/water-pump-control")
    public ResponseEntity<Map<String, String>> controlWaterPump(@PathVariable String deviceId) {
        log.info("📱 프론트엔드로부터 물펌프 가동 요청 접수 - 디바이스 ID: {}", deviceId);

        // MqttService의 펌프 가동 로직 호출 (이전 세션에서 구현한 메서드)
        deviceControlService.turnOnWaterPump(deviceId);

        // 프론트엔드에게 성공 메시지 JSON 반환
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "물펌프 제어 명령이 디바이스로 성공적으로 발송되었습니다."
        ));
    }

    /**
     * 5. 영양제(보충제) 공급기 제어 API
     */
    @PostMapping("/{deviceId}/supplement-control")
    public ResponseEntity<Map<String, String>> controlSupplement(@PathVariable String deviceId) {
        log.info("📱 프론트엔드로부터 영양제 공급 요청 접수 - 디바이스 ID: {}", deviceId);

        // MqttService에 추가될 영양제 공급 로직 호출
        deviceControlService.turnOnSupplement(deviceId);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "영양제 공급 제어 명령이 디바이스로 성공적으로 발송되었습니다."
        ));
    }

    // 중복 로직 방지를 위한 변환 메서드
    private SensorDataResponseDto.SensorLogDetailDto convertToDetailDto(SensorLog log) {
        return SensorDataResponseDto.SensorLogDetailDto.builder()
                .temperature(log.getTemperature())
                .humidity(log.getHumidity())
                .soilMoisture(log.getSoilMoisture())
                .illuminance(log.getIlluminance())
                .isAbnormal(log.getIsAbnormal())
                .createdAt(log.getCreatedAt())
                .build();
    }
}