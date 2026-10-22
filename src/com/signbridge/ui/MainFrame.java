package com.signbridge.ui;

import com.signbridge.model.User;
import com.signbridge.service.AuthService;
import com.signbridge.ui.screens.*;
import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    public static final String SCREEN_LOGIN = "LOGIN";
    public static final String SCREEN_DASHBOARD = "DASHBOARD";
    public static final String SCREEN_LEARN = "LEARN";
    public static final String SCREEN_QUIZ = "QUIZ";
    public static final String SCREEN_MEMORY = "MEMORY";
    public static final String SCREEN_SPEED = "SPEED";
    public static final String SCREEN_PROFILE = "PROFILE";

    private final CardLayout cardLayout;
    private final JPanel screenContainer;
    private final JPanel topNav;
    private final JLabel lblNavUser;
    private final JLabel lblNavLevel;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private final LoginScreen loginScreen;
    private final DashboardScreen dashboardScreen;
    private final LearnScreen learnScreen;
    private final QuizScreen quizScreen;
    private final MemoryGameScreen memoryScreen;
    private final SpeedGameScreen speedScreen;
    private final ProfileScreen profileScreen;

    public MainFrame() {
        super("SignBridge - Sign Language Learning & Gaming App (BCA Project)");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 740);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());

        // ================= TOP NAVIGATION BAR =================
        // Always visible except on the login/signup screen, so every
        // screen in the app can always get back to the Dashboard.
        topNav = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(15, 22, 38));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        topNav.setPreferredSize(new Dimension(0, 56));
        topNav.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Brand logo (left) - simple colored badge, no emoji/font dependency
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
        brandPanel.setOpaque(false);
        JLabel brandBadge = UITheme.createBadge("SB", UITheme.PRIMARY);
        JLabel brandText = new JLabel("SIGNBRIDGE");
        brandText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandText.setForeground(UITheme.TEXT_LIGHT);
        brandPanel.add(brandBadge);
        brandPanel.add(brandText);
        topNav.add(brandPanel, BorderLayout.WEST);

        // Center navigation links
        JPanel navLinks = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 10));
        navLinks.setOpaque(false);

        String[][] tabs = {
                {"Dashboard", SCREEN_DASHBOARD},
                {"Dictionary", SCREEN_LEARN},
                {"Quiz Arena", SCREEN_QUIZ},
                {"Memory Match", SCREEN_MEMORY},
                {"Speed Blitz", SCREEN_SPEED},
                {"Profile", SCREEN_PROFILE}
        };

        for (String[] t : tabs) {
            final String scrName = t[1];
            JButton btn = new JButton(t[0]);
            btn.setFont(UITheme.FONT_BOLD);
            btn.setForeground(UITheme.TEXT_MUTED);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> showScreen(scrName));
            navButtons.put(scrName, btn);
            navLinks.add(btn);
        }
        topNav.add(navLinks, BorderLayout.CENTER);

        // Right: current user + level badge
        JPanel userWidget = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        userWidget.setOpaque(false);

        lblNavLevel = UITheme.createBadge("LVL 1", UITheme.CYAN);
        lblNavUser = new JLabel("Guest");
        lblNavUser.setFont(UITheme.FONT_BOLD);
        lblNavUser.setForeground(UITheme.TEXT_LIGHT);

        userWidget.add(lblNavLevel);
        userWidget.add(lblNavUser);
        topNav.add(userWidget, BorderLayout.EAST);

        add(topNav, BorderLayout.NORTH);

        // ================= SCREEN CONTAINER =================
        cardLayout = new CardLayout();
        screenContainer = new JPanel(cardLayout);
        screenContainer.setBackground(UITheme.BG_DARK);

        // Instantiate screens
        loginScreen = new LoginScreen(this);
        dashboardScreen = new DashboardScreen(this);
        learnScreen = new LearnScreen(this);
        quizScreen = new QuizScreen(this);
        memoryScreen = new MemoryGameScreen(this);
        speedScreen = new SpeedGameScreen(this);
        profileScreen = new ProfileScreen(this);

        // Register screens
        screenContainer.add(loginScreen, SCREEN_LOGIN);
        screenContainer.add(dashboardScreen, SCREEN_DASHBOARD);
        screenContainer.add(learnScreen, SCREEN_LEARN);
        screenContainer.add(quizScreen, SCREEN_QUIZ);
        screenContainer.add(memoryScreen, SCREEN_MEMORY);
        screenContainer.add(speedScreen, SCREEN_SPEED);
        screenContainer.add(profileScreen, SCREEN_PROFILE);

        add(screenContainer, BorderLayout.CENTER);

        // Start on login
        showScreen(SCREEN_LOGIN);
    }

    public void showScreen(String screenName) {
        // Top nav is hidden only on the login/signup screen
        topNav.setVisible(!SCREEN_LOGIN.equals(screenName));

        // Highlight the active tab
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(screenName)) {
                entry.getValue().setForeground(UITheme.PRIMARY);
                entry.getValue().setFont(new Font("Segoe UI", Font.BOLD, 15));
            } else {
                entry.getValue().setForeground(UITheme.TEXT_MUTED);
                entry.getValue().setFont(UITheme.FONT_BOLD);
            }
        }

        // Update the nav bar's user/level badge
        User u = AuthService.getInstance().getCurrentUser();
        if (u != null) {
            lblNavUser.setText(u.getFullName());
            lblNavLevel.setText("LVL " + u.getCurrentLevel());
        }

        // Refresh data whenever a screen becomes visible
        switch (screenName) {
            case SCREEN_DASHBOARD:
                dashboardScreen.refreshData();
                break;
            case SCREEN_LEARN:
                learnScreen.refreshData();
                break;
            case SCREEN_MEMORY:
                memoryScreen.startNewGame();
                break;
            case SCREEN_PROFILE:
                profileScreen.refreshData();
                break;
        }

        cardLayout.show(screenContainer, screenName);
    }

    public void onLoginSuccess() {
        showScreen(SCREEN_DASHBOARD);
    }
}
