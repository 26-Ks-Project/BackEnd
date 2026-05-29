package com.example.smart_farm.domain.quest.dto;

import com.example.smart_farm.domain.quest.entity.Quest;
import com.example.smart_farm.domain.quest.entity.QuestStatus;

public record QuestResponseDTO(
        Long id,
        String title,
        String description,
        Integer rewardPoints, // 경험치/보상
        QuestStatus status,    // 현재 상태
        boolean isCompleted    // 완료 여부 (상태가 COMPLETED인지 확인)
) {
    // Entity를 DTO로 변환하는 정적 팩토리 메서드
    public static QuestResponseDTO from(Quest quest) {
        return new QuestResponseDTO(
                quest.getId(),
                quest.getTitle(),
                quest.getDescription(),
                quest.getRewardPoints(),
                quest.getStatus(),
                quest.getStatus() == QuestStatus.COMPLETED
        );
    }
}
