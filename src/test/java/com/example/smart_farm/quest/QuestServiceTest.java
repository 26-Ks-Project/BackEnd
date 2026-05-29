package com.example.smart_farm.quest;

import com.example.smart_farm.domain.quest.dto.UserDailyQuestResponseDto;
import com.example.smart_farm.domain.quest.entity.Quest;
import com.example.smart_farm.domain.quest.entity.QuestStatus;
import com.example.smart_farm.domain.quest.entity.UserDailyQuest;
import com.example.smart_farm.domain.quest.repository.UserDailyQuestRepository;
import com.example.smart_farm.domain.quest.service.QuestService;
import com.example.smart_farm.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QuestServiceTest {

    @Mock
    private UserDailyQuestRepository userDailyQuestRepository;

    @InjectMocks
    private QuestService questService;

    private User testUser;
    private List<UserDailyQuest> tempDailyQuests;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("password")
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        Quest q1 = createQuest(1L, "물 주기", 100);
        Quest q2 = createQuest(2L, "비료 주기", 200);
        Quest q3 = createQuest(3L, "수확하기", 300);

        UserDailyQuest udq1 = createUserDailyQuest(1L, testUser, q1, false);
        UserDailyQuest udq2 = createUserDailyQuest(2L, testUser, q2, true);
        UserDailyQuest udq3 = createUserDailyQuest(3L, testUser, q3, false);

        tempDailyQuests = List.of(udq1, udq2, udq3);
    }

    private Quest createQuest(Long id, String title, int rewardPoints) {
        Quest quest = Quest.builder()
                .title(title)
                .description(title + " 설명")
                .status(QuestStatus.ACTIVE)
                .iconType("WATER")
                .rewardPoints(rewardPoints)
                .build();
        ReflectionTestUtils.setField(quest, "id", id);
        return quest;
    }

    private UserDailyQuest createUserDailyQuest(Long id, User user, Quest quest, boolean isCompleted) {
        UserDailyQuest userDailyQuest = UserDailyQuest.builder()
                .user(user)
                .quest(quest)
                .isCompleted(isCompleted)
                .assignedDate(LocalDate.now())
                .build();
        ReflectionTestUtils.setField(userDailyQuest, "id", id);
        return userDailyQuest;
    }

    @Test
    @DisplayName("성공: 특정 유저의 오늘 자 전체 일일 퀘스트를 조회한다")
    void getTodayAllQuests_Success() {
        // given
        given(userDailyQuestRepository.findByUser_IdAndAssignedDate(1L, LocalDate.now()))
                .willReturn(tempDailyQuests);

        // when
        List<UserDailyQuestResponseDto> result = questService.getTodayAllQuests(1L);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).title()).isEqualTo("물 주기");
    }

    @Test
    @DisplayName("성공: 특정 유저의 오늘 자 활성화된(미완료) 일일 퀘스트만 조회한다")
    void getTodayActiveQuests_Success() {
        // given
        given(userDailyQuestRepository.findByUser_IdAndAssignedDate(1L, LocalDate.now()))
                .willReturn(tempDailyQuests);

        // when
        List<UserDailyQuestResponseDto> result = questService.getTodayActiveQuests(1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(dto -> !dto.isCompleted());
    }

    @Test
    @DisplayName("성공: 특정 유저의 일일 퀘스트를 완료 처리한다")
    void completeQuest_Success() {
        // given
        UserDailyQuest udq = tempDailyQuests.get(0); // 1번 퀘스트 (미완료)
        given(userDailyQuestRepository.findById(1L)).willReturn(Optional.of(udq));

        // when
        questService.completeQuest(1L, 1L);

        // then
        assertThat(udq.isCompleted()).isTrue();
        assertThat(udq.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("예외: 존재하지 않는 일일 퀘스트 ID로 완료 요청 시 IllegalArgumentException이 발생한다")
    void completeQuest_NotFound_Exception() {
        // given
        given(userDailyQuestRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> questService.completeQuest(1L, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 일일 퀘스트가 존재하지 않습니다");
    }

    @Test
    @DisplayName("예외: 다른 유저의 퀘스트를 완료 처리하려고 하면 IllegalArgumentException이 발생한다")
    void completeQuest_Unauthorized_Exception() {
        // given
        UserDailyQuest udq = tempDailyQuests.get(0); // user_id = 1L
        given(userDailyQuestRepository.findById(1L)).willReturn(Optional.of(udq));

        // when & then
        assertThatThrownBy(() -> questService.completeQuest(2L, 1L)) // 2번 유저가 요청
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인에게 할당된 퀘스트만 완료할 수 있습니다");
    }

    @Test
    @DisplayName("예외: 이미 완료된 퀘스트를 다시 완료 처리하려고 하면 IllegalStateException이 발생한다")
    void completeQuest_AlreadyCompleted_Exception() {
        // given
        UserDailyQuest udq = tempDailyQuests.get(1); // 2번 퀘스트 (이미 완료됨)
        given(userDailyQuestRepository.findById(2L)).willReturn(Optional.of(udq));

        // when & then
        assertThatThrownBy(() -> questService.completeQuest(1L, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 완료된 퀘스트입니다");
    }
}
