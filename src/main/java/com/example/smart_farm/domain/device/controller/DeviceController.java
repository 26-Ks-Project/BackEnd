package com.example.smart_farm.domain.device.controller;

import com.example.smart_farm.domain.device.dto.SensorAvgResponse;
import com.example.smart_farm.domain.device.dto.SensorDataResponseDto;
import com.example.smart_farm.domain.device.entity.SensorLog;
import com.example.smart_farm.domain.device.repository.SensorLogRepository;
import com.example.smart_farm.domain.device.service.SensorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final SensorLogRepository sensorLogRepository;
    private final SensorLogService sensorLogService;

    /**
     * 1. 실시간 센서 데이터 조회 (카드 폴링용 - 최신 1건)
     * 5초마다 호출 시 DB 부하를 최소화하기 위해 딱 한 건만 반환합니다. [cite: 35, 48-49]
     */
    @GetMapping("/{deviceId}/sensors")
    public ResponseEntity<SensorDataResponseDto> getLatestSensorData(@PathVariable String deviceId) {

        // Repository에서 가장 최근 데이터 1건만 조회
        SensorLog latestLog = sensorLogRepository.findTopByDeviceIdOrderByCreatedAtDesc(deviceId)
                .orElseThrow(() -> new RuntimeException("해당 디바이스의 데이터가 존재하지 않습니다."));

        SensorDataResponseDto.SensorLogDetailDto detail = convertToDetailDto(latestLog);

        return ResponseEntity.ok(SensorDataResponseDto.builder()
                .deviceId(deviceId)
                .data(List.of(detail)) // [cite: 42] 명세서 형식을 맞추기 위해 리스트로 감쌉니다.
                .build());
    }

    /**
     * 2. 센서 히스토리 조회 (그래프 초기 로딩용)
     * 처음 진입 시 과거 트렌드를 그리기 위해 여러 건을 한 번에 가져옵니다.
     */
    @GetMapping("/{deviceId}/history")
    public ResponseEntity<SensorDataResponseDto> getSensorHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "100") int limit) {

        // PageRequest를 Pageable 타입으로 전달하여 에러 방지
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
     * 특정 기기의 오늘(00:00~23:59) 2시간 단위 센서 평균 데이터 조회
     */
    @GetMapping("/{deviceId}/today-avg")
    public ResponseEntity<List<SensorAvgResponse>> getTodayAverages(@PathVariable String deviceId) {
        List<SensorAvgResponse> data = sensorLogService.getTodayTwoHourAverages(deviceId);
        return ResponseEntity.ok(data);
    }

    // 중복 로직 방지를 위한 변환 메서드 [cite: 44-49]
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