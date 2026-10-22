package com.signbridge.ui.screens;

import com.signbridge.db.dao.AchievementDAO;
import com.signbridge.db.dao.GameDAO;
import com.signbridge.db.dao.QuizDAO;
import com.signbridge.model.User;
import com.signbridge.service.AuthService;
import com.signbridge.ui.MainFrame;
import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class DashboardScreen extends JPanel {
    private final MainFrame mainFrame;
    private final JLabel lblWelcome;
    private final JLabel lblLevel;
    private final JLabel lblXp;
    private final JProgressBar xpBar;
    private final JLabel lblStatQuizzes;
    private final JLabel lblStatAccuracy;
    private final JLabel lblStatMemory;
    private final JLabel lblStatBadges;

    public DashboardScreen(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(24, 32, 24, 32));

        // Top Banner: User Profile Header
        JPanel headerCard = new JPanel(new BorderLayout(16, 16));
        headerCard.setBackground(UITheme.CARD_BG);
        headerCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JPanel userMeta = new JPanel(new GridLayout(2, 1, 4, 4));
        userMeta.setOpaque(false);
        lblWelcome = new JLabel("Welcome back, Aayushi!");
        lblWelcome.setFont(UITheme.FONT_TITLE);
        lblWelcome.setForeground(UITheme.TEXT_LIGHT);

        lblLevel = new JLabel("Level 2 Sign Explorer");
        lblLevel.setFont(UITheme.FONT_BOLD);
        lblLevel.setForeground(UITheme.PRIMARY);

        userMeta.add(lblWelcome);
        userMeta.add(lblLevel);
        headerCard.add(userMeta, BorderLayout.WEST);

        // XP Progress
        JPanel xpPanel = new JPanel(new BorderLayout(6, 6));
        xpPanel.setOpaque(false);
        xpPanel.setPreferredSize(new Dimension(240, 48));

        lblXp = new JLabel("XP: 0 / 250", SwingConstants.RIGHT);
        lblXp.setFont(UITheme.FONT_SMALL);
        lblXp.setForeground(UITheme.TEXT_MUTED);

        xpBar = new JProgressBar(0, 100);
        xpBar.setValue(0);
        xpBar.setPreferredSize(new Dimension(240, 12));
        xpBar.setForeground(UITheme.SUCCESS);
        xpBar.setBackground(UITheme.SURFACE);
        xpBar.setBorderPainted(false);

        xpPanel.add(lblXp, BorderLayout.NORTH);
        xpPanel.add(xpBar, BorderLayout.SOUTH);
        headerCard.add(xpPanel, BorderLayout.EAST);

        add(headerCard, BorderLayout.NORTH);

        // Center Content: Stats row + Action Hub
        JPanel centerContainer = new JPanel(new BorderLayout(20, 20));
        centerContainer.setOpaque(false);

        // 1. Stats Row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 85));

        lblStatQuizzes = new JLabel("0", SwingConstants.CENTER);
        lblStatAccuracy = new JLabel("0%", SwingConstants.CENTER);
        lblStatMemory = new JLabel("0", SwingConstants.CENTER);
        lblStatBadges = new JLabel("0 / 5", SwingConstants.CENTER);

        statsRow.add(createMiniStatCard("Quizzes Taken", lblStatQuizzes, UITheme.CYAN));
        statsRow.add(createMiniStatCard("Overall Accuracy", lblStatAccuracy, UITheme.SUCCESS));
        statsRow.add(createMiniStatCard("Memory High Score", lblStatMemory, UITheme.WARNING));
        statsRow.add(createMiniStatCard("Badges Earned", lblStatBadges, UITheme.PURPLE));

        centerContainer.add(statsRow, BorderLayout.NORTH);

        // 2. Navigation Actions Grid
        JPanel navGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        navGrid.setOpaque(false);

        navGrid.add(createActionTile("Learn & Explore",
                "Explore A-Z alphabets, numbers 0-9, and everyday words with interactive flashcards and finger placement guides.",
                UITheme.PRIMARY,
                () -> mainFrame.showScreen(MainFrame.SCREEN_LEARN)));

        navGrid.add(createActionTile("Quiz Arena",
                "Test your sign skills with timed multiple-choice questions. Real-time feedback, timers, and XP rewards.",
                UITheme.CYAN,
                () -> mainFrame.showScreen(MainFrame.SCREEN_QUIZ)));

        navGrid.add(createActionTile("Memory Match",
                "Flip cards and match hand signs with their meanings. Sharpen your visual recognition and memory!",
                UITheme.WARNING,
                () -> mainFrame.showScreen(MainFrame.SCREEN_MEMORY)));

        navGrid.add(createActionTile("Speed Blitz (Time Attack)",
                "30-second rapid challenge! Identify as many signs as possible under time pressure to climb the leaderboard.",
                UITheme.DANGER,
                () -> mainFrame.showScreen(MainFrame.SCREEN_SPEED)));

        centerContainer.add(navGrid, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createMiniStatCard(String label, JLabel valLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLabel.setForeground(accent);

        JLabel lblTitle = new JLabel(label, SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        card.add(valLabel, BorderLayout.CENTER);
        card.add(lblTitle, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createActionTile(String title, String desc, Color accent, Runnable action) {
        JPanel tile = new JPanel(new BorderLayout(10, 10));
        tile.setBackground(UITheme.CARD_BG);
        tile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tile.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(accent);

        JTextArea txtDesc = new JTextArea(desc);
        txtDesc.setFont(UITheme.FONT_BODY);
        txtDesc.setForeground(UITheme.TEXT_LIGHT);
        txtDesc.setBackground(UITheme.CARD_BG);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setEditable(false);
        txtDesc.setFocusable(false);

        JButton btnOpen = UITheme.createPrimaryButton("Launch");
        btnOpen.setBackground(accent);
        btnOpen.addActionListener(e -> action.run());

        tile.add(lblTitle, BorderLayout.NORTH);
        tile.add(txtDesc, BorderLayout.CENTER);
        tile.add(btnOpen, BorderLayout.SOUTH);

        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { action.run(); }
            @Override
            public void mouseEntered(MouseEvent e) {
                tile.setBackground(UITheme.CARD_HOVER);
                txtDesc.setBackground(UITheme.CARD_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                tile.setBackground(UITheme.CARD_BG);
                txtDesc.setBackground(UITheme.CARD_BG);
            }
        });

        return tile;
    }

    public void refreshData() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;

        lblWelcome.setText("Welcome back, " + user.getFullName() + "!");
        lblLevel.setText("Level " + user.getCurrentLevel() + " Sign Explorer");
        lblXp.setText("Total XP: " + user.getTotalXp() + " (Level " + user.getCurrentLevel() + ")");
        xpBar.setValue(user.getLevelProgressPercent());

        // Refresh stats from DB
        QuizDAO qDao = new QuizDAO();
        Map<String, Object> qSummary = qDao.getUserQuizSummary(user.getId());
        lblStatQuizzes.setText(String.valueOf(qSummary.get("totalQuizzes")));
        lblStatAccuracy.setText(qSummary.get("accuracy") + "%");

        GameDAO gDao = new GameDAO();
        int bestMemory = gDao.getUserBestScore(user.getId(), "Memory Match");
        lblStatMemory.setText(String.valueOf(bestMemory));

        AchievementDAO aDao = new AchievementDAO();
        long unlockedCount = aDao.getAllAchievementsForUser(user.getId()).stream().filter(a -> a.isUnlocked()).count();
        lblStatBadges.setText(unlockedCount + " / 5");
    }
}
