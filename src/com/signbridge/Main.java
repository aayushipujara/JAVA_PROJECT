package com.signbridge;

import com.signbridge.db.DatabaseManager;
import com.signbridge.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SignBridge - Sign Language Learning & Gaming   ");
        System.out.println("   BCA Academic Subject Project - Java Desktop    ");
        System.out.println("==================================================");

        // 1. Set modern Swing Look & Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[Main] Using default look and feel: " + e.getMessage());
        }

        // 2. Initialize Database & Seed data
        System.out.println("[Main] Initializing Database connection and tables...");
        DatabaseManager.initializeDatabase();

        // 3. Launch UI on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
            System.out.println("[Main] SignBridge UI successfully launched!");
        });
    }
}
