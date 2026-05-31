package com.example.smart_farm.domain.device.controller;

import com.example.smart_farm.domain.device.dto.SensorAvgResponse;
import com.example.smart_farm.domain.device.dto.SensorDataResponseDto;
import com.example.smart_farm.domain.device.dto.SensorHistoryResponse;
import com.example.smart_farm.domain.device.service.DeviceControlService;
import com.example.smart_farm.domain.device.service.SensorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // 프론트 연동 대비 CORS 추가
public class DeviceController {

    private final SensorLogService sensorLogService;
    private final DeviceControlService deviceControlService;

    /**
     * 1. 실시간 센서 데이터 조회 (카드 폴링용 - 최신 1건)
     */
    @GetMapping("/{deviceId}/sensors")
    public ResponseEntity<SensorDataResponseDto> getLatestSensorData(@PathVariable String deviceId) {
        log.info("📱 실시간 센서 데이터 조회 요청 - 디바이스 ID: {}", deviceId);
        SensorDataResponseDto.SensorLogDetailDto detail = sensorLogService.getLatestSensorData(deviceId);

        return ResponseEntity.ok(SensorDataResponseDto.builder()
                .deviceId(deviceId)
                .data(List.of(detail))
                .build());
    }

    /**
     * 2. 센서 히스토리 조회 (그래프 초기 로딩용 - 최근 N개)
     */
    @GetMapping("/{deviceId}/history")
    public ResponseEntity<SensorDataResponseDto> getSensorHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "100") int limit) {
        log.info("📱 센서 최근 히스토리 조회 요청 - 디바이스 ID: {}, 제한 건수: {}", deviceId, limit);
        List<SensorDataResponseDto.SensorLogDetailDto> dataList = sensorLogService.getSensorHistory(deviceId, limit);

        return ResponseEntity.ok(SensorDataResponseDto.builder()
                .deviceId(deviceId)
                .data(dataList)
                .build());
    }

    /**
     * 3. 특정 날짜의 하루 전체 센서 로그 조회 API
     * 예: GET /api/v1/devices/device-123/2026-05-31/history
     */
    @GetMapping("/{deviceId}/{day}/history")
    public ResponseEntity<SensorHistoryResponse> getSensorHistoryByDay(
            @PathVariable String deviceId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day) {
        log.info("📱 특정 날짜 센서 로그 조회 요청 - 디바이스 ID: {}, 조회 날짜: {}", deviceId, day);
        List<SensorDataResponseDto.SensorLogDetailDto> dataList = sensorLogService.getSensorLogsByDay(deviceId, day);

        return ResponseEntity.ok(SensorHistoryResponse.builder()
                .deviceId(deviceId)
                .searchDate(day.toString())
                .logs(dataList)
                .build());
    }

    /**
     * 4. 특정 기기의 오늘(00:00~23:59) 2시간 단위 센서 평균 데이터 조회
     */
    @GetMapping("/{deviceId}/today-avg")
    public ResponseEntity<List<SensorAvgResponse>> getTodayAverages(@PathVariable String deviceId) {
        log.info("📱 오늘 센서 2시간 평균값 조회 요청 - 디바이스 ID: {}", deviceId);
        List<SensorAvgResponse> data = sensorLogService.getTodayTwoHourAverages(deviceId);
        return ResponseEntity.ok(data);
    }

    /**
     * 5. 물펌프 제어 API
     */
    @PostMapping("/{deviceId}/water-pump-control")
    public ResponseEntity<Map<String, String>> controlWaterPump(@PathVariable String deviceId) {
        log.info("📱 프론트엔드로부터 물펌프 가동 요청 접수 - 디바이스 ID: {}", deviceId);
        deviceControlService.turnOnWaterPump(deviceId);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "물펌프 제어 명령이 디바이스로 성공적으로 발송되었습니다."
        ));
    }

    /**
     * 6. 영양제(보충제) 공급기 제어 API
     */
    @PostMapping("/{deviceId}/supplement-control")
    public ResponseEntity<Map<String, String>> controlSupplement(@PathVariable String deviceId) {
        log.info("📱 프론트엔드로부터 영양제 공급 요청 접수 - 디바이스 ID: {}", deviceId);
        deviceControlService.turnOnSupplement(deviceId);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "영양제 공급 제어 명령이 디바이스로 성공적으로 발송되었습니다."
        ));
    }
}