package com.amongthesloths.trophybot.models;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UserTrophy {
    private long userId;
    private String username;
    private int trophyId;
    private LocalDateTime awardedAt;
    private long awardedBy;
    private Trophy trophy;
    private Timestamp awardDate;

    public UserTrophy(long userId, String username, int trophyId, LocalDateTime awardedAt, long awardedBy) {
        this.userId = userId;
        this.username = username;
        this.trophyId = trophyId;
        this.awardedAt = awardedAt;
        this.awardedBy = awardedBy;
    }

    public UserTrophy(Trophy trophy, Timestamp awardDate) {
        this.trophy = trophy;
        this.awardDate = awardDate;
    }

    // Getters and Setters
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTrophyId() {
        return trophyId;
    }

    public void setTrophyId(int trophyId) {
        this.trophyId = trophyId;
    }

    public LocalDateTime getAwardedAt() {
        return awardedAt;
    }

    public void setAwardedAt(LocalDateTime awardedAt) {
        this.awardedAt = awardedAt;
    }

    public long getAwardedBy() {
        return awardedBy;
    }

    public void setAwardedBy(long awardedBy) {
        this.awardedBy = awardedBy;
    }

    public Trophy getTrophy() {
        return trophy;
    }

    public void setTrophy(Trophy trophy) {
        this.trophy = trophy;
    }

    public Timestamp getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(Timestamp awardDate) {
        this.awardDate = awardDate;
    }
} 