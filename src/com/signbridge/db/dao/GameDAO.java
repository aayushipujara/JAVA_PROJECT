package com.signbridge.db.dao;

import com.signbridge.db.DBConnection;
import com.signbridge.model.GameScore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    public boolean saveScore(int userId, String gameName, int score, int moves, int timeSeconds) {
        String sql = "INSERT INTO game_scores (user_id, game_name, score, moves, time_taken_seconds) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, gameName);
            pstmt.setInt(3, score);
            pstmt.setInt(4, moves);
            pstmt.setInt(5, timeSeconds);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[GameDAO] Error saving game score: " + e.getMessage());
            return false;
        }
    }

    public List<GameScore> getTopScores(String gameName, int limit) {
        List<GameScore> list = new ArrayList<>();
        String sql = "SELECT g.id, g.user_id, u.username, g.game_name, g.score, g.moves, g.time_taken_seconds, g.played_at " +
                     "FROM game_scores g " +
                     "JOIN users u ON g.user_id = u.id " +
                     "WHERE g.game_name = ? " +
                     "ORDER BY g.score DESC, g.time_taken_seconds ASC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, gameName);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new GameScore(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("game_name"),
                            rs.getInt("score"),
                            rs.getInt("moves"),
                            rs.getInt("time_taken_seconds"),
                            rs.getTimestamp("played_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[GameDAO] Error fetching top scores: " + e.getMessage());
        }
        return list;
    }

    public int getUserBestScore(int userId, String gameName) {
        String sql = "SELECT MAX(score) FROM game_scores WHERE user_id = ? AND game_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, gameName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[GameDAO] Error fetching best score: " + e.getMessage());
        }
        return 0;
    }
}
