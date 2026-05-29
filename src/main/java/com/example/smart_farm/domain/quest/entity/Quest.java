package com.example.smart_farm.domain.quest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "quests")
public class Quest { // 생성, 수정 시간 자동 관리 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // 퀘스트 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 퀘스트 내용/설명

    @Column(name = "icon_type", nullable = false, length = 50)
    private String iconType; // 🎯 QuestType 대신 추가된 아이콘 타입 (문자열)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestStatus status; // 퀘스트 상태

    @Column(nullable = false)
    private Integer rewardPoints; // 완료 시 지급할 포인트/보상

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 비즈니스 로직: 퀘스트 정보 수정
    public void updateQuest(String title, String description, Integer rewardPoints) {
        this.title = title;
        this.description = description;
        this.rewardPoints = rewardPoints;
    }

    // 퀘스트 상태 변경
    public void changeStatus(QuestStatus status) {
        this.status = status;
    }
}