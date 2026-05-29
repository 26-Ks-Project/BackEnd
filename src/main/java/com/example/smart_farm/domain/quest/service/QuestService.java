package com.example.smart_farm.domain.quest.service;

import com.example.smart_farm.domain.quest.dto.UserDailyQuestResponseDto;
import com.example.smart_farm.domain.quest.entity.UserDailyQuest;
import com.example.smart_farm.domain.quest.repository.UserDailyQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본 읽기 전용 (성능 최적화)
public class QuestService {

    private final UserDailyQuestRepository userDailyQuestRepository;

    /**
     * 1. 특정 유저의 오늘 자 일일 퀘스트 전체 목록 조회
     */
    public List<UserDailyQuestResponseDto> getTodayAllQuests(Long userId) {
        return userDailyQuestRepository.findByUser_IdAndAssignedDate(userId, LocalDate.now())
                .stream()
                .map(UserDailyQuestResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 2. 특정 유저의 오늘 자 일일 퀘스트 중 '진행 중(미완료)'인 퀘스트만 조회
     */
    public List<UserDailyQuestResponseDto> getTodayActiveQuests(Long userId) {
        return userDailyQuestRepository.findByUser_IdAndAssignedDate(userId, LocalDate.now())
                .stream()
                .filter(userDailyQuest -> !userDailyQuest.isCompleted()) // 완료되지 않은 퀘스트만 필터링
                .map(UserDailyQuestResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 3. 특정 유저의 특정 일일 퀘스트 완료 처리
     */
    @Transactional // 쓰기 작업 보장
    public void completeQuest(Long userId, Long dailyQuestId) {
        // 일일 퀘스트 매핑 데이터 조회
        UserDailyQuest dailyQuest = userDailyQuestRepository.findById(dailyQuestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일일 퀘스트가 존재하지 않습니다. id=" + dailyQuestId));

        // 보안 검증: 다른 유저가 가로채서 완료할 수 없도록 차단
        if (!dailyQuest.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인에게 할당된 퀘스트만 완료할 수 있습니다.");
        }

        // 중복 완료 방지
        if (dailyQuest.isCompleted()) {
            throw new IllegalStateException("이미 완료된 퀘스트입니다.");
        }

        // 엔티티 내부 비즈니스 로직 호출 (isCompleted = true 및 시간 업데이트)
        dailyQuest.complete();

        // 별도의 save() 호출 없이도 영속성 컨텍스트의 Dirty Checking에 의해 자동 반영됩니다.
    }
}