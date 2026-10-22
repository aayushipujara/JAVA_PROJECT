package com.signbridge.ui.screens;

import com.signbridge.db.dao.AchievementDAO;
import com.signbridge.db.dao.GameDAO;
import com.signbridge.db.dao.QuizDAO;
import com.signbridge.model.Achievement;
import com.signbridge.model.GameScore;
import com.signbridge.model.User;
import com.signbridge.service.AuthService;
import com.signbridge.ui.MainFrame;
import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class ProfileScreen extends JPanel {
    private final MainFrame mainFrame;
    private final AchievementDAO achievementDAO;
    private final QuizDAO quizDAO;
    private final GameDAO gameDAO;

    private final JLabel lblName;
    private final JLabel lblUser;
    private final JLabel lblXp;
    private final JLabel lblLevel;
    private final JProgressBar levelBar;
    private final JPanel badgesContainer;
    private final DefaultTableModel quizTableModel;
    private final DefaultTableModel scoreTableModel;

    public ProfileScreen(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.achievementDAO = new AchievementDAO();
        this.quizDAO = new QuizDAO();
        this.gameDAO = new GameDAO();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 28, 16, 28));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JButton btnBack = UITheme.createSecondaryButton("< Dashboard");
        btnBack.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD));
        topBar.add(btnBack, BorderLayout.WEST);

        JLabel title = new JLabel("Student Profile & Achievements");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.PRIMARY);
        topBar.add(title, BorderLayout.CENTER);

        JButton btnLogout = UITheme.createSecondaryButton("Logout");
        btnLogout.addActionListener(e -> {
            AuthService.getInstance().logout();
            mainFrame.showScreen(MainFrame.SCREEN_LOGIN);
        });
        topBar.add(btnLogout, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center: Profile Header Card + Tabbed Pane (Badges, Quiz History, Game High Scores)
        JPanel centerPanel = new JPanel(new BorderLayout(14, 14));
        centerPanel.setOpaque(false);

        // Profile Header Card
        JPanel profileHeader = new JPanel(new BorderLayout(16, 16));
        profileHeader.setBackground(UITheme.CARD_BG);
        profileHeader.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true), new EmptyBorder(16, 20, 16, 20)));

        JPanel userMeta = new JPanel(new GridLayout(3, 1, 4, 4));
        userMeta.setOpaque(false);
        lblName = new JLabel("Aayushi");
        lblName.setFont(UITheme.FONT_TITLE);
        lblName.setForeground(UITheme.TEXT_LIGHT);

        lblUser = new JLabel("@aayushi");
        lblUser.setFont(UITheme.FONT_BODY);
        lblUser.setForeground(UITheme.TEXT_MUTED);

        lblLevel = new JLabel("Level 1");
        lblLevel.setFont(UITheme.FONT_BOLD);
        lblLevel.setForeground(UITheme.CYAN);

        userMeta.add(lblName);
        userMeta.add(lblUser);
        userMeta.add(lblLevel);
        profileHeader.add(userMeta, BorderLayout.WEST);

        // XP bar
        JPanel xpBox = new JPanel(new BorderLayout(6, 6));
        xpBox.setOpaque(false);
        xpBox.setPreferredSize(new Dimension(280, 50));
        lblXp = new JLabel("Total XP: 0", SwingConstants.RIGHT);
        lblXp.setFont(UITheme.FONT_SMALL);
        lblXp.setForeground(UITheme.WARNING);

        levelBar = new JProgressBar(0, 100);
        levelBar.setForeground(UITheme.SUCCESS);
        levelBar.setBackground(UITheme.SURFACE);
        levelBar.setBorderPainted(false);
        xpBox.add(lblXp, BorderLayout.NORTH);
        xpBox.add(levelBar, BorderLayout.SOUTH);
        profileHeader.add(xpBox, BorderLayout.EAST);

        centerPanel.add(profileHeader, BorderLayout.NORTH);

        // Tabbed Pane for Badges, Quiz History, Leaderboards
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(UITheme.CARD_BG);
        tabbedPane.setForeground(UITheme.TEXT_LIGHT);
        tabbedPane.setFont(UITheme.FONT_BOLD);

        // 1. Badges Tab
        badgesContainer = new JPanel(new GridLayout(1, 5, 14, 14));
        badgesContainer.setBackground(UITheme.BG_DARK);
        badgesContainer.setBorder(new EmptyBorder(16, 16, 16, 16));
        tabbedPane.addTab("Achievements", badgesContainer);

        // 2. Quiz History Tab
        String[] quizCols = {"Category", "Score", "Total Qs", "Accuracy", "Time (sec)", "Date"};
        quizTableModel = new DefaultTableModel(quizCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable quizTable = createStyledTable(quizTableModel);
        JScrollPane quizScroll = new JScrollPane(quizTable);
        quizScroll.setBackground(UITheme.BG_DARK);
        quizScroll.getViewport().setBackground(UITheme.BG_DARK);
        quizScroll.setBorder(null);
        tabbedPane.addTab("Quiz History", quizScroll);

        // 3. Game Scores Tab
        String[] scoreCols = {"Game Name", "Score", "Moves / Correct", "Time Taken", "Date"};
        scoreTableModel = new DefaultTableModel(scoreCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable scoreTable = createStyledTable(scoreTableModel);
        JScrollPane scoreScroll = new JScrollPane(scoreTable);
        scoreScroll.setBackground(UITheme.BG_DARK);
        scoreScroll.getViewport().setBackground(UITheme.BG_DARK);
        scoreScroll.setBorder(null);
        tabbedPane.addTab("Game High Scores", scoreScroll);

        centerPanel.add(tabbedPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(UITheme.CARD_BG);
        table.setForeground(UITheme.TEXT_LIGHT);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(32);
        table.setGridColor(UITheme.BORDER_COLOR);
        table.getTableHeader().setBackground(UITheme.SURFACE);
        table.getTableHeader().setForeground(UITheme.TEXT_LIGHT);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        return table;
    }

    public void refreshData() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;

        lblName.setText(user.getFullName());
        lblUser.setText("@" + user.getUsername());
        lblLevel.setText("Level " + user.getCurrentLevel() + " Explorer");
        lblXp.setText("Total XP: " + user.getTotalXp());
        levelBar.setValue(user.getLevelProgressPercent());

        // 1. Refresh Badges
        badgesContainer.removeAll();
        List<Achievement> achievements = achievementDAO.getAllAchievementsForUser(user.getId());
        for (Achievement a : achievements) {
            JPanel badgeCard = new JPanel(new BorderLayout(8, 8));
            badgeCard.setBackground(a.isUnlocked() ? UITheme.CARD_BG : UITheme.SURFACE);
            badgeCard.setBorder(new CompoundBorder(
                    new LineBorder(a.isUnlocked() ? UITheme.WARNING : UITheme.BORDER_COLOR, a.isUnlocked() ? 2 : 1, true),
                    new EmptyBorder(12, 10, 12, 10)
            ));

            JPanel iconHolder = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    UITheme.drawVectorIcon(g2, a.getIconType(), getWidth() / 2 - 16, 4, 32, a.isUnlocked() ? UITheme.WARNING : UITheme.TEXT_MUTED);
                    g2.dispose();
                }
            };
            iconHolder.setPreferredSize(new Dimension(60, 42));
            iconHolder.setOpaque(false);
            JLabel lblTitle = new JLabel(a.getTitle(), SwingConstants.CENTER);
            lblTitle.setFont(UITheme.FONT_BOLD);
            lblTitle.setForeground(a.isUnlocked() ? UITheme.WARNING : UITheme.TEXT_MUTED);

            JTextArea txtDesc = new JTextArea(a.getDescription());
            txtDesc.setFont(UITheme.FONT_SMALL);
            txtDesc.setForeground(UITheme.TEXT_MUTED);
            txtDesc.setBackground(badgeCard.getBackground());
            txtDesc.setLineWrap(true);
            txtDesc.setWrapStyleWord(true);
            txtDesc.setEditable(false);

            badgeCard.add(iconHolder, BorderLayout.NORTH);
            badgeCard.add(lblTitle, BorderLayout.CENTER);
            badgeCard.add(txtDesc, BorderLayout.SOUTH);
            badgesContainer.add(badgeCard);
        }
        badgesContainer.revalidate();
        badgesContainer.repaint();

        // 2. Refresh Quiz History Table
        quizTableModel.setRowCount(0);
        List<Map<String, Object>> quizHistory = quizDAO.getUserQuizHistory(user.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Map<String, Object> row : quizHistory) {
            int score = (int) row.get("score");
            int total = (int) row.get("totalQuestions");
            double acc = total > 0 ? (score * 100.0 / total) : 0;
            String dateStr = row.get("attemptedAt") != null ? sdf.format(row.get("attemptedAt")) : "-";

            quizTableModel.addRow(new Object[]{
                    row.get("category"),
                    score,
                    total,
                    Math.round(acc) + "%",
                    row.get("timeSeconds") + "s",
                    dateStr
            });
        }

        // 3. Refresh Game Scores Table
        scoreTableModel.setRowCount(0);
        List<GameScore> memoryScores = gameDAO.getTopScores("Memory Match", 5);
        List<GameScore> speedScores = gameDAO.getTopScores("Speed Blitz", 5);

        for (GameScore gs : memoryScores) {
            String dateStr = gs.getPlayedAt() != null ? sdf.format(gs.getPlayedAt()) : "-";
            scoreTableModel.addRow(new Object[]{gs.getGameName(), gs.getScore(), gs.getMoves() + " moves", gs.getTimeTakenSeconds() + "s", dateStr});
        }
        for (GameScore gs : speedScores) {
            String dateStr = gs.getPlayedAt() != null ? sdf.format(gs.getPlayedAt()) : "-";
            scoreTableModel.addRow(new Object[]{gs.getGameName(), gs.getScore(), gs.getMoves() + " correct", gs.getTimeTakenSeconds() + "s", dateStr});
        }
    }
}
