package com.example.smart_farm.domain.quest.scheduler;

import com.example.smart_farm.domain.quest.entity.Quest;
import com.example.smart_farm.domain.quest.entity.UserDailyQuest;
import com.example.smart_farm.domain.quest.repository.QuestRepository;
import com.example.smart_farm.domain.quest.repository.UserDailyQuestRepository;
import com.example.smart_farm.domain.quest.service.QuestService;
import com.example.smart_farm.domain.user.entity.User;
import com.example.smart_farm.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestScheduler {
    private final QuestService questService;

    /**
     * 매일 자정(00:00:00)에 실행되는 크론탭
     * 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void assignDailyQuests() {
        log.info("⏰ 자정 스케줄러 가동: 일일 퀘스트 배정 프로세스를 트리거합니다.");

        // 스케줄러 레이어에는 트랜잭션(@Transactional)도, 레포지토리도 두지 않고 서비스에 전임합니다.
        questService.processDailyQuestAssignment();

        log.info("✅ 일일 퀘스트 배정 프로세스 종료.");
    }
}
