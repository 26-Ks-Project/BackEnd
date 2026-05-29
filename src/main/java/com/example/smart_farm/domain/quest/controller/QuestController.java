package com.example.smart_farm.domain.quest.controller;

import com.example.smart_farm.domain.quest.dto.UserDailyQuestResponseDto;
import com.example.smart_farm.domain.quest.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quests") // REST 규칙에 맞춰 복수형(quests)으로 추천합니다!
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;

    /**
     * 1. 특정 유저의 오늘 자 일일 퀘스트 전체 목록 조회
     * GET /api/v1/quests/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserDailyQuestResponseDto>> getTodayAllQuests(@PathVariable Long userId) {
        List<UserDailyQuestResponseDto> quests = questService.getTodayAllQuests(userId);
        return ResponseEntity.ok(quests);
    }

    /**
     * 2. 특정 유저의 오늘 자 일일 퀘스트 중 미완료(진행 중) 퀘스트만 조회
     * GET /api/v1/quests/user/{userId}/active
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<UserDailyQuestResponseDto>> getTodayActiveQuests(@PathVariable Long userId) {
        List<UserDailyQuestResponseDto> quests = questService.getTodayActiveQuests(userId);
        return ResponseEntity.ok(quests);
    }

    /**
     * 3. 특정 유저의 특정 일일 퀘스트 완료 처리
     * PATCH /api/v1/quests/user/{userId}/complete/{dailyQuestId}
     */
    @PatchMapping("/user/{userId}/complete/{dailyQuestId}")
    public ResponseEntity<Void> completeQuest(
            @PathVariable Long userId,
            @PathVariable Long dailyQuestId) {

        questService.completeQuest(userId, dailyQuestId);
        return ResponseEntity.noContent().build(); // 성공 시 204 No Content 반환
    }
}