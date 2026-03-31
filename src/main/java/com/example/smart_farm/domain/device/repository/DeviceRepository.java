package com.example.smart_farm.domain.device.repository;

import com.example.smart_farm.domain.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    // 기본 상속만으로도 findById(String id)를 사용할 수 있네!
}