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
        @Index(name = "idx_device_time", columnList = "device_id, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(name = "soil_moisture", precision = 10, scale = 2)
    private BigDecimal soilMoisture;

    private Integer illuminance;

    @Column(name = "is_abnormal")
    private Boolean isAbnormal = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public SensorLog(Device device, BigDecimal temperature, BigDecimal humidity,
                     BigDecimal soilMoisture, Integer illuminance, Boolean isAbnormal,
                     LocalDateTime createdAt) { // 1. 여기에 파라미터 추가
        this.device = device;
        this.temperature = temperature;
        this.humidity = humidity;
        this.soilMoisture = soilMoisture;
        this.illuminance = illuminance;
        this.isAbnormal = isAbnormal != null ? isAbnormal : false;
        // 2. 파라미터로 받은 createdAt을 필드에 할당 (null이면 현재 시간)
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }
}