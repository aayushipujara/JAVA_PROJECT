package com.signbridge.model;

import java.sql.Timestamp;

public class Achievement {
    private String key;
    private String title;
    private String description;
    private String iconEmoji;
    private boolean isUnlocked;
    private Timestamp unlockedAt;

    public Achievement(String key, String title, String description, String iconEmoji) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.isUnlocked = false;
    }

    public String getKey() { return key; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getIconEmoji() { return iconEmoji; }
    public String getIconType() { return iconEmoji; }
    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
    public Timestamp getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Timestamp unlockedAt) { this.unlockedAt = unlockedAt; }
}
