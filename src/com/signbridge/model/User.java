package com.signbridge.model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private int totalXp;
    private int currentLevel;
    private Timestamp createdAt;

    public User() {}

    public User(int id, String username, String passwordHash, String fullName, int totalXp, int currentLevel, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.totalXp = totalXp;
        this.currentLevel = currentLevel;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }

    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getXpForNextLevel() {
        return currentLevel * 250;
    }

    public int getLevelProgressPercent() {
        int currentLevelFloor = (currentLevel - 1) * 250;
        int inLevelXp = Math.max(0, totalXp - currentLevelFloor);
        return Math.min(100, (int) ((inLevelXp / 250.0) * 100));
    }
}
