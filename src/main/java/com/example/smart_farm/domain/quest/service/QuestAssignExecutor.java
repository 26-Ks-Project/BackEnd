package com.example.smart_farm.domain.quest.service;

import com.example.smart_farm.domain.quest.entity.Quest;
import com.example.smart_farm.domain.quest.entity.UserDailyQuest;
import com.example.smart_farm.domain.quest.repository.UserDailyQuestRepository;
import com.example.smart_farm.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestAssignExecutor {

    private final UserDailyQuestRepository userDailyQuestRepository;

    // 💡 REQUIRES_NEW를 보장하기 위해 별도 컴포넌트로 분리하여 트랜잭션 벽을 세웁니다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void assignToUser(User user, List<Quest> selectedQuests, LocalDate today) {
        if (userDailyQuestRepository.existsByUser_IdAndAssignedDate(user.getId(), today)) {
            return;
        }

        for (Quest quest : selectedQuests) {
            UserDailyQuest dailyQuest = UserDailyQuest.builder()
                    .user(user)
                    .quest(quest)
                    .assignedDate(today)
                    .isCompleted(false)
                    .build();
            userDailyQuestRepository.save(dailyQuest);
        }
    }
}