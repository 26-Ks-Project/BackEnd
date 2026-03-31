package com.example.smart_farm.domain.device.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class SensorLogDataDto {
    private String deviceId;          // farmId(Long) -> deviceId(String) 변경 [cite: 28]
    private BigDecimal temperature;   // double -> BigDecimal (DECIMAL(5,2) 매핑) [cite: 29]
    private BigDecimal humidity;      // double -> BigDecimal (DECIMAL(5,2) 매핑) [cite: 29]
    private BigDecimal soilMoisture;  // double -> BigDecimal (DECIMAL(5,2) 매핑) [cite: 30]
    private Integer illuminance;      // INT 매핑 [cite: 31]
}