package com.example.smart_farm.domain.quest.repository;

import com.example.smart_farm.domain.quest.entity.UserDailyQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserDailyQuestRepository extends JpaRepository<UserDailyQuest, Long> {
    // 특정 유저에게 오늘 이미 할당된 퀘스트가 있는지 확인용
    boolean existsByUser_IdAndAssignedDate(Long userId, LocalDate date);

    List<UserDailyQuest> findByUser_IdAndAssignedDate(Long userId, LocalDate date);
}
