package com.example.smart_farm.domain.quest.dto;

import com.example.smart_farm.domain.quest.entity.UserDailyQuest;
import lombok.Builder;

@Builder
public record UserDailyQuestResponseDto(
        Long dailyQuestId, // 일일 퀘스트 고유 ID (완료 처리 요청 시 사용)
        Long questId,      // 원본 퀘스트 ID
        String title,
        String description,
        int xpReward,
        String iconType,
        boolean isCompleted
) {
    public static UserDailyQuestResponseDto from(UserDailyQuest userDailyQuest) {
        return UserDailyQuestResponseDto.builder()
                .dailyQuestId(userDailyQuest.getId())
                .questId(userDailyQuest.getQuest().getId())
                .title(userDailyQuest.getQuest().getTitle())
                .description(userDailyQuest.getQuest().getDescription())
                .xpReward(userDailyQuest.getQuest().getRewardPoints())
                .iconType(userDailyQuest.getQuest().getIconType())
                .isCompleted(userDailyQuest.isCompleted())
                .build();
    }
}
