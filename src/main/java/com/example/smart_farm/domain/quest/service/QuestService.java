package com.example.smart_farm.domain.quest.service;

import com.example.smart_farm.domain.quest.dto.UserDailyQuestResponseDto;
import com.example.smart_farm.domain.quest.entity.Quest;
import com.example.smart_farm.domain.quest.entity.UserDailyQuest;
import com.example.smart_farm.domain.quest.repository.QuestRepository;
import com.example.smart_farm.domain.quest.repository.UserDailyQuestRepository;
import com.example.smart_farm.domain.user.entity.User;
import com.example.smart_farm.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // 기본 읽기 전용 (성능 최적화)
public class QuestService {

    private final UserDailyQuestRepository userDailyQuestRepository;
    private final UserRepository userRepository;
    private final QuestRepository questRepository;
    private final QuestAssignExecutor questAssignExecutor;

    @Transactional
    public List<UserDailyQuestResponseDto> getTodayAllQuests(Long userId) {
        LocalDate today = LocalDate.now();

        // 1. 먼저 오늘 자 할당된 퀘스트가 있는지 조회
        List<UserDailyQuest> dailyQuests = userDailyQuestRepository.findByUser_IdAndAssignedDate(userId, today);

        // 2. 자정 배치 누락자 또는 신규 가입자이므로 실시간 구제 로직 가동
        if (dailyQuests.isEmpty()) {
            log.warn("⚠️ 유저 ID {}번의 오늘 자 퀘스트가 존재하지 않습니다. 실시간 동적 할당을 시작합니다.", userId);

            // 유저 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + userId));

            // 마스터 퀘스트 테이블에서 전체 목록 로드
            List<Quest> allQuests = questRepository.findAll();
            if (allQuests.size() < 4) {
                log.error("❌ 실시간 배정 실패: 마스터 퀘스트 개수가 4개 미만입니다.");
                throw new IllegalStateException("시스템 내에 배정 가능한 퀘스트가 부족합니다.");
            }

            // 무작위 4개 추출
            List<Quest> shuffleTarget = new ArrayList<>(allQuests);
            Collections.shuffle(shuffleTarget);
            List<Quest> selectedQuests = shuffleTarget.stream()
                    .limit(4)
                    .collect(Collectors.toList());

            // 💡 중요: 외부 컴포넌트(Executor)를 호출하여 REQUIRES_NEW 트랜잭션으로 안전하게 저장
            questAssignExecutor.assignToUser(user, selectedQuests, today);

            // 3. 저장이 완료되었으므로 DB에서 최종 데이터 재조회
            dailyQuests = userDailyQuestRepository.findByUser_IdAndAssignedDate(userId, today);
        }

        // 4. 최종 데이터를 DTO로 변환하여 응답
        return dailyQuests.stream()
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

        User user = dailyQuest.getUser();
        Quest quest = dailyQuest.getQuest(); // UserDailyQuest 내에 Quest 연관관계가 있다고 가정합니다.

        if (quest != null && quest.getRewardPoints() != null) {
            int rewardXp = quest.getRewardPoints();
            user.addXp(rewardXp); // 유저 엔티티의 xp 필드 증가
        }
    }

    @Transactional(readOnly = true) // 데이터 로드 중심이므로 readOnly 설정
    public void processDailyQuestAssignment() {
        LocalDate today = LocalDate.now();

        List<Quest> allQuests = questRepository.findAll();
        if (allQuests.size() < 4) {
            log.error("❌ 배정 실패: 전체 퀘스트 개수가 4개 미만입니다. DB를 확인하세요.");
            return;
        }

        List<Quest> shuffleTarget = new ArrayList<>(allQuests);
        int page = 0;
        int pageSize = 100;
        Page<User> userPage;

        do {
            userPage = userRepository.findAll(PageRequest.of(page, pageSize));

            for (User user : userPage.getContent()) {
                try {
                    Collections.shuffle(shuffleTarget);
                    List<Quest> selectedQuests = shuffleTarget.stream()
                            .limit(4)
                            .collect(Collectors.toList());

                    // 외부 컴포넌트 호출을 통해 REQUIRES_NEW 트랜잭션이 정상 작동함
                    questAssignExecutor.assignToUser(user, selectedQuests, today);
                } catch (Exception e) {
                    log.error("❌ 유저 ID {}번 퀘스트 배정 중 에러 발생 (건너뜀): {}", user.getId(), e.getMessage());
                }
            }
            page++;
        } while (userPage.hasNext());
    }
}