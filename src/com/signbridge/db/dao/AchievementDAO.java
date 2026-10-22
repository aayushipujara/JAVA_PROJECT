package com.signbridge.db.dao;

import com.signbridge.db.DBConnection;
import com.signbridge.model.Achievement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementDAO {

    private static final List<Achievement> CATALOG = new ArrayList<>();

    static {
        CATALOG.add(new Achievement("FIRST_QUIZ", "First Steps", "Completed your very first Sign Language quiz.", "MEDAL"));
        CATALOG.add(new Achievement("QUIZ_ACE", "Sign Genius", "Achieved a perfect 100% score on a quiz.", "STAR"));
        CATALOG.add(new Achievement("MEMORY_PRO", "Memory Master", "Won the Memory Match game in under 20 moves.", "MEDAL"));
        CATALOG.add(new Achievement("SPEED_RUNNER", "Speed Demon", "Scored 100+ points in Speed Blitz Time Attack.", "STAR"));
        CATALOG.add(new Achievement("LEVEL_5", "Sign Scholar", "Reached Level 5 in SignBridge.", "MEDAL"));
    }

    public List<Achievement> getAllAchievementsForUser(int userId) {
        Map<String, java.sql.Timestamp> unlockedMap = new HashMap<>();
        String sql = "SELECT badge_key, unlocked_at FROM user_badges WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    unlockedMap.put(rs.getString("badge_key"), rs.getTimestamp("unlocked_at"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[AchievementDAO] Error fetching badges: " + e.getMessage());
        }

        List<Achievement> result = new ArrayList<>();
        for (Achievement a : CATALOG) {
            Achievement copy = new Achievement(a.getKey(), a.getTitle(), a.getDescription(), a.getIconEmoji());
            if (unlockedMap.containsKey(a.getKey())) {
                copy.setUnlocked(true);
                copy.setUnlockedAt(unlockedMap.get(a.getKey()));
            }
            result.add(copy);
        }
        return result;
    }

    public boolean unlockBadge(int userId, String badgeKey) {
        String sql = "INSERT OR IGNORE INTO user_badges (user_id, badge_key) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, badgeKey);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AchievementDAO] Error unlocking badge: " + e.getMessage());
            return false;
        }
    }
}
