package com.signbridge.db.dao;

import com.signbridge.db.DBConnection;
import com.signbridge.model.SignItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SignDAO {

    public List<SignItem> getAllSigns() {
        List<SignItem> list = new ArrayList<>();
        String sql = "SELECT id, symbol, category, description, gesture_type, hint FROM signs ORDER BY category, id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapSign(rs));
            }
        } catch (SQLException e) {
            System.err.println("[SignDAO] Error fetching all signs: " + e.getMessage());
        }
        return list;
    }

    public List<SignItem> getSignsByCategory(String category) {
        List<SignItem> list = new ArrayList<>();
        String sql = "SELECT id, symbol, category, description, gesture_type, hint FROM signs WHERE category = ? ORDER BY id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSign(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[SignDAO] Error fetching signs by category: " + e.getMessage());
        }
        return list;
    }

    public SignItem getSignBySymbol(String symbol) {
        String sql = "SELECT id, symbol, category, description, gesture_type, hint FROM signs WHERE UPPER(symbol) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapSign(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[SignDAO] Error fetching sign by symbol: " + e.getMessage());
        }
        return null;
    }

    public List<SignItem> getRandomSigns(int count, String category) {
        List<SignItem> list = new ArrayList<>();
        String sql;
        boolean filterCategory = category != null && !category.equalsIgnoreCase("All");

        if (filterCategory) {
            sql = "SELECT id, symbol, category, description, gesture_type, hint FROM signs WHERE category = ? ORDER BY RANDOM() LIMIT ?";
        } else {
            sql = "SELECT id, symbol, category, description, gesture_type, hint FROM signs ORDER BY RANDOM() LIMIT ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (filterCategory) {
                pstmt.setString(1, category);
                pstmt.setInt(2, count);
            } else {
                pstmt.setInt(1, count);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSign(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[SignDAO] Error fetching random signs: " + e.getMessage());
        }
        return list;
    }

    private SignItem mapSign(ResultSet rs) throws SQLException {
        return new SignItem(
                rs.getInt("id"),
                rs.getString("symbol"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("gesture_type"),
                rs.getString("hint")
        );
    }
}
