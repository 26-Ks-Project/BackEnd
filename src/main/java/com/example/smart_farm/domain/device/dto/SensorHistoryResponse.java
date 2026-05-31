package com.example.smart_farm.domain.device.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SensorHistoryResponse {
    private String deviceId;
    private String searchDate; // YYYY-MM-DD 포맷 문자열
    private List<SensorDataResponseDto.SensorLogDetailDto> logs;
}
