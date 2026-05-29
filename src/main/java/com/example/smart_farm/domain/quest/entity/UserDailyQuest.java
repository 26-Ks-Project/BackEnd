package com.example.smart_farm.domain.quest.entity;

import com.example.smart_farm.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_daily_quest",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_daily_assignment",
                        columnNames = {"user_id", "quest_id", "assigned_date"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserDailyQuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    private Quest quest;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // --- 비즈니스 로직 ---

    /**
     * 퀘스트 클리어 처리
     */
    public void complete() {
        if (!this.isCompleted) {
            this.isCompleted = true;
            this.completedAt = LocalDateTime.now();
        }
    }
}
