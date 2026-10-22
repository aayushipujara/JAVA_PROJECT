package com.signbridge.db.dao;

import com.signbridge.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizDAO {

    public boolean saveQuizResult(int userId, String category, int score, int totalQuestions, int timeTakenSeconds) {
        String sql = "INSERT INTO quiz_results (user_id, category, score, total_questions, time_taken_seconds) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, category);
            pstmt.setInt(3, score);
            pstmt.setInt(4, totalQuestions);
            pstmt.setInt(5, timeTakenSeconds);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[QuizDAO] Error saving quiz result: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getUserQuizHistory(int userId) {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT category, score, total_questions, time_taken_seconds, attempted_at " +
                     "FROM quiz_results WHERE user_id = ? ORDER BY attempted_at DESC LIMIT 10";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("category", rs.getString("category"));
                    map.put("score", rs.getInt("score"));
                    map.put("totalQuestions", rs.getInt("total_questions"));
                    map.put("timeSeconds", rs.getInt("time_taken_seconds"));
                    map.put("attemptedAt", rs.getTimestamp("attempted_at"));
                    results.add(map);
                }
            }
        } catch (SQLException e) {
            System.err.println("[QuizDAO] Error fetching quiz history: " + e.getMessage());
        }
        return results;
    }

    public Map<String, Object> getUserQuizSummary(int userId) {
        Map<String, Object> summary = new HashMap<>();
        String sql = "SELECT COUNT(*) AS total_quizzes, COALESCE(SUM(score), 0) AS total_score, " +
                     "COALESCE(SUM(total_questions), 0) AS total_answered FROM quiz_results WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int totalQuizzes = rs.getInt("total_quizzes");
                    int totalScore = rs.getInt("total_score");
                    int totalAnswered = rs.getInt("total_answered");
                    double accuracy = totalAnswered > 0 ? (totalScore * 100.0 / totalAnswered) : 0.0;

                    summary.put("totalQuizzes", totalQuizzes);
                    summary.put("totalScore", totalScore);
                    summary.put("accuracy", Math.round(accuracy * 10.0) / 10.0);
                    return summary;
                }
            }
        } catch (SQLException e) {
            System.err.println("[QuizDAO] Error calculating quiz summary: " + e.getMessage());
        }
        summary.put("totalQuizzes", 0);
        summary.put("totalScore", 0);
        summary.put("accuracy", 0.0);
        return summary;
    }
}
