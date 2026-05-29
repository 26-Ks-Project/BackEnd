package com.example.smart_farm.domain.quest.repository;

import com.example.smart_farm.domain.quest.entity.Quest;
import com.example.smart_farm.domain.quest.entity.QuestStatus;
import com.example.smart_farm.domain.quest.entity.QuestType;
import com.example.smart_farm.domain.quest.entity.UserDailyQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {
    // 1. 특정 상태의 퀘스트 목록 조회 (예: 진행 중인 퀘스트만 보기)
    List<Quest> findByStatus(QuestStatus status);


    // 2. 전체 퀘스트 목록을 조회
    List<Quest> findAll();
}
