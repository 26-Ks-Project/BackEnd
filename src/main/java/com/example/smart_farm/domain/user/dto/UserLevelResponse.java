package com.example.smart_farm.domain.user.dto;

import com.example.smart_farm.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserLevelResponse {
    private final int xp;
    private final int level;
    private final int nextLevelXp;    // 다음 레벨이 되기 위해 필요한 총 경험치
    private final int remainingXp;  // 다음 레벨까지 남은 경험치

    public UserLevelResponse(User user) {
        this.xp = user.getXp();

        // 🎯 레벨 계산 공식: 100xp마다 1레벨 상승 (0~99xp = 1레벨, 100~199xp = 2레벨...)
        this.level = (user.getXp() / 100) + 1;

        // 다음 레벨을 찍기 위한 기준점 xp (예: 2레벨이 되려면 100xp 필요)
        this.nextLevelXp = (this.level) * 100;

        // 다음 레벨까지 남은 잔여 xp
        this.remainingXp = this.nextLevelXp - this.xp;
    }
}