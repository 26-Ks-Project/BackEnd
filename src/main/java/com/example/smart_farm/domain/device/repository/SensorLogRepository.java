package com.example.smart_farm.domain.device.repository;

import com.example.smart_farm.domain.device.entity.Device;
import com.example.smart_farm.domain.device.entity.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {
    // 1. 기기 ID(String)로 가장 최근 로그 하나 찾기
    Optional<SensorLog> findTopByDeviceIdOrderByCreatedAtDesc(String deviceId);

    // 2. 또는 Device 객체 자체로 찾고 싶다면 이렇게도 가능하네
    Optional<SensorLog> findTopByDeviceOrderByCreatedAtDesc(Device device);
}
