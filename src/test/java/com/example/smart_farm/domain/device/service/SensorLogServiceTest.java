package com.example.smart_farm.domain.device.service;

import com.example.smart_farm.domain.device.dto.SensorDataResponseDto;
import com.example.smart_farm.domain.device.entity.SensorLog;
import com.example.smart_farm.domain.device.repository.SensorLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorLogServiceTest {

    @Mock
    private SensorLogRepository sensorLogRepository;

    @InjectMocks
    private SensorLogService sensorLogService;

    @Test
    @DisplayName("특정 날짜의 센서 데이터를 정상적으로 조회하여 반환한다")
    void getSensorLogsByDay_success() {
        // given
        String deviceId = "device-123";
        LocalDate searchDate = LocalDate.of(2026, 5, 31);
        LocalDateTime start = searchDate.atStartOfDay();
        LocalDateTime end = searchDate.atTime(LocalTime.MAX);

        SensorLog mockLog = SensorLog.builder()
                .device(null)
                .temperature(new BigDecimal("25.50"))
                .humidity(new BigDecimal("60.00"))
                .soilMoisture(new BigDecimal("45.20"))
                .illuminance(500)
                .isAbnormal(false)
                .createdAt(start.plusHours(10))
                .build();

        when(sensorLogRepository.findByDeviceIdAndCreatedAtBetween(eq(deviceId), eq(start), eq(end)))
                .thenReturn(List.of(mockLog));

        // when
        List<SensorDataResponseDto.SensorLogDetailDto> result = sensorLogService.getSensorLogsByDay(deviceId, searchDate);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTemperature()).isEqualTo(new BigDecimal("25.50"));
        assertThat(result.get(0).getHumidity()).isEqualTo(new BigDecimal("60.00"));
        assertThat(result.get(0).getSoilMoisture()).isEqualTo(new BigDecimal("45.20"));
        assertThat(result.get(0).getIlluminance()).isEqualTo(500);
        assertThat(result.get(0).getCreatedAt()).isEqualTo(start.plusHours(10));
    }
}
