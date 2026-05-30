package com.example.smart_farm.domain.quest.scheduler;

import com.example.smart_farm.domain.quest.entity.Quest;
import com.example.smart_farm.domain.quest.entity.UserDailyQuest;
import com.example.smart_farm.domain.quest.repository.QuestRepository;
import com.example.smart_farm.domain.quest.repository.UserDailyQuestRepository;
import com.example.smart_farm.domain.user.entity.User;
import com.example.smart_farm.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestScheduler {

    private final UserRepository userRepository;
    private final QuestRepository questRepository;
    private final UserDailyQuestRepository userDailyQuestRepository;

    /**
     * 매일 자정(00:00:00)에 실행되는 크론탭
     * 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void assignDailyQuests() {
        log.info("⏰ 자정 스케줄러 가동: 모든 유저에게 일일 퀘스트 배정을 시작합니다.");
        LocalDate today = LocalDate.now();

        // 1. 마스터 테이블에서 전체 퀘스트 ID 목록 로드
        List<Quest> allQuests = questRepository.findAll();
        if (allQuests.size() < 4) {
            log.error("❌ 배정 실패: 전체 퀘스트 개수가 4개 미만입니다. DB를 확인하세요.");
            return;
        }

        // 2. 전체 서비스 유저 목록 로드
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                // 중복 실행 방지: 오늘 이미 할당된 유저라면 패스
                if (userDailyQuestRepository.existsByUser_IdAndAssignedDate(user.getId(), today)) {
                    continue;
                }

                // 3. 퀘스트 ID 목록을 무작위로 섞고 딱 4개만 추출
                Collections.shuffle(allQuests);
                List<Quest> selectedQuests = allQuests.stream()
                        .limit(4)
                        .collect(Collectors.toList());

                // 4. 유저별 일일 퀘스트 데이터 생성 및 저장
                for (Quest quest : selectedQuests) {
                    UserDailyQuest dailyQuest = UserDailyQuest.builder()
                            .user(user)
                            .quest(quest)
                            .assignedDate(today)
                            .isCompleted(false)
                            .build();

                    userDailyQuestRepository.save(dailyQuest);
                }
            } catch (Exception e) {
                log.error("❌ 유저 ID {}번 퀘스트 배정 중 에러 발생: {}", user.getId(), e.getMessage());
                // 특정 유저가 실패해도 다른 유저들은 계속 배정되어야 하므로 try-catch로 감싸줍니다.
            }
        }

        log.info("✅ 일일 퀘스트 배정 완료 완료 완료!");
    }
}
