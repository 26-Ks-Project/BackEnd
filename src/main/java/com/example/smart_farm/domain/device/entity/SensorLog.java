package com.example.smart_farm.domain.device.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_logs", indexes = {
        @Index(name = "idx_device_time", columnList = "device_id, created_at") // 조회 최적화 인덱스 [cite: 37]
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id; // [cite: 28]

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device; // [cite: 28, 35]

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature; // 온도 [cite: 29]

    @Column(precision = 5, scale = 2)
    private BigDecimal humidity; // 습도 [cite: 29]

    @Column(name = "soil_moisture", precision = 5, scale = 2)
    private BigDecimal soilMoisture; // 토양 수분 [cite: 30]

    private Integer illuminance; // 조도 [cite: 31]

    @Column(name = "is_abnormal")
    private Boolean isAbnormal = false; // 이상값 여부 [cite: 33]

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // [cite: 34]

    @Builder
    public SensorLog(Device device, BigDecimal temperature, BigDecimal humidity,
                     BigDecimal soilMoisture, Integer illuminance, Boolean isAbnormal) {
        this.device = device;
        this.temperature = temperature;
        this.humidity = humidity;
        this.soilMoisture = soilMoisture;
        this.illuminance = illuminance;
        this.isAbnormal = isAbnormal;
    }
}
