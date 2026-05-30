package com.example.smart_farm.domain.user.service;

import com.example.smart_farm.domain.user.dto.UserLevelResponse;
import com.example.smart_farm.domain.user.entity.User;
import com.example.smart_farm.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStatusService {

    private final UserRepository userRepository;

    /**
     * 유저의 현재 경험치 및 레벨 정보 조회
     */
    public UserLevelResponse getUserLevelInfo(Long userId) {
        // 1. 데이터베이스에서 유저 조회 (없으면 예외 발생)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. id=" + userId));

        // 2. 조회된 엔티티를 레벨 연산 로직이 포함된 DTO로 변환하여 반환
        return new UserLevelResponse(user);
    }
}