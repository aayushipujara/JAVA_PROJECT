package com.signbridge.ui.screens;

import com.signbridge.db.dao.SignDAO;
import com.signbridge.model.SignItem;
import com.signbridge.service.AuthService;
import com.signbridge.service.GameService;
import com.signbridge.ui.MainFrame;
import com.signbridge.ui.components.GestureRenderer;
import com.signbridge.ui.components.TimerBar;
import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpeedGameScreen extends JPanel {
    private final MainFrame mainFrame;
    private final GameService gameService;
    private final SignDAO signDAO;

    private List<SignItem> signPool;
    private SignItem currentSign;
    private int score = 0;
    private int correctCount = 0;
    private int streak = 0;
    private int secondsLeft = 30;
    private Timer speedTimer;

    private final JLabel lblTimerNum;
    private final JLabel lblScore;
    private final JLabel lblStreak;
    private final TimerBar timerBar;
    private final GestureRenderer gestureRenderer;
    private final JButton[] optionButtons = new JButton[3];

    private final CardLayout cardLayout;
    private final JPanel container;
    private final JPanel startPanel;
    private final JPanel activePanel;

    public SpeedGameScreen(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.gameService = new GameService();
        this.signDAO = new SignDAO();

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        container.setOpaque(false);

        // 1. Start Screen
        startPanel = new JPanel(new GridBagLayout());
        startPanel.setBackground(UITheme.BG_DARK);

        JPanel startBox = new JPanel(new BorderLayout(16, 20));
        startBox.setBackground(UITheme.CARD_BG);
        startBox.setPreferredSize(new Dimension(460, 380));
        startBox.setBorder(new CompoundBorder(new LineBorder(UITheme.DANGER, 2, true), new EmptyBorder(24, 28, 24, 28)));

        JLabel lblTitle = new JLabel("Speed Blitz (Time Attack)", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.DANGER);
        startBox.add(lblTitle, BorderLayout.NORTH);

        JTextArea txtRules = new JTextArea("HOW TO PLAY:\n\n- You have 30 SECONDS on the clock.\n- Identify hand signs as quickly as you can.\n- Build streaks for score multipliers!\n- Saves your high score directly to database.");
        txtRules.setFont(UITheme.FONT_BODY);
        txtRules.setForeground(UITheme.TEXT_LIGHT);
        txtRules.setBackground(UITheme.CARD_BG);
        txtRules.setEditable(false);
        txtRules.setLineWrap(true);
        txtRules.setWrapStyleWord(true);
        startBox.add(txtRules, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);

        JButton btnBack = UITheme.createSecondaryButton("< Dashboard");
        btnBack.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD));

        JButton btnStart = UITheme.createPrimaryButton("Start Blitz!");
        btnStart.setBackground(UITheme.DANGER);
        btnStart.addActionListener(e -> startBlitz());

        btnRow.add(btnBack);
        btnRow.add(btnStart);
        startBox.add(btnRow, BorderLayout.SOUTH);

        startPanel.add(startBox);
        container.add(startPanel, "START");

        // 2. Active Blitz Screen
        activePanel = new JPanel(new BorderLayout(12, 12));
        activePanel.setBackground(UITheme.BG_DARK);
        activePanel.setBorder(new EmptyBorder(16, 32, 16, 32));

        // Top Header
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);

        JPanel statRow = new JPanel(new BorderLayout());
        statRow.setOpaque(false);

        lblTimerNum = new JLabel("30s");
        lblTimerNum.setFont(UITheme.FONT_TITLE);
        lblTimerNum.setForeground(UITheme.DANGER);

        lblStreak = new JLabel("Streak: 0", SwingConstants.CENTER);
        lblStreak.setFont(UITheme.FONT_BOLD);
        lblStreak.setForeground(UITheme.WARNING);

        lblScore = new JLabel("Score: 0", SwingConstants.RIGHT);
        lblScore.setFont(UITheme.FONT_TITLE);
        lblScore.setForeground(UITheme.SUCCESS);

        statRow.add(lblTimerNum, BorderLayout.WEST);
        statRow.add(lblStreak, BorderLayout.CENTER);
        statRow.add(lblScore, BorderLayout.EAST);
        header.add(statRow, BorderLayout.NORTH);

        timerBar = new TimerBar(30);
        header.add(timerBar, BorderLayout.SOUTH);
        activePanel.add(header, BorderLayout.NORTH);

        // Center Gesture
        JPanel centerArea = new JPanel(new BorderLayout(16, 16));
        centerArea.setOpaque(false);

        JPanel renderWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        renderWrap.setOpaque(false);
        gestureRenderer = new GestureRenderer(null, 220, 190);
        renderWrap.add(gestureRenderer);
        centerArea.add(renderWrap, BorderLayout.CENTER);

        // 3 Options
        JPanel optionsRow = new JPanel(new GridLayout(1, 3, 16, 0));
        optionsRow.setOpaque(false);
        optionsRow.setPreferredSize(new Dimension(0, 60));

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            optionButtons[i] = new JButton("Choice");
            optionButtons[i].setFont(new Font("Segoe UI", Font.BOLD, 22));
            optionButtons[i].setForeground(UITheme.TEXT_LIGHT);
            optionButtons[i].setBackground(UITheme.CARD_BG);
            optionButtons[i].setFocusPainted(false);
            optionButtons[i].setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true), new EmptyBorder(10, 12, 10, 12)));
            optionButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            optionButtons[i].addActionListener(e -> handleOptionClicked(idx));
            optionsRow.add(optionButtons[i]);
        }
        centerArea.add(optionsRow, BorderLayout.SOUTH);
        activePanel.add(centerArea, BorderLayout.CENTER);

        container.add(activePanel, "ACTIVE");

        add(container, BorderLayout.CENTER);
    }

    public void startBlitz() {
        signPool = signDAO.getAllSigns();
        if (signPool.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No sign data found in database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        score = 0;
        correctCount = 0;
        streak = 0;
        secondsLeft = 30;

        lblScore.setText("Score: 0");
        lblStreak.setText("Streak: 0 ");
        lblTimerNum.setText(" 30s");
        timerBar.setMaximum(300);
        timerBar.setValue(300);

        cardLayout.show(container, "ACTIVE");
        nextQuestion();

        if (speedTimer != null) speedTimer.stop();
        speedTimer = new Timer(100, e -> {
            timerBar.setValue(timerBar.getValue() - 1);
            if (timerBar.getValue() % 10 == 0) {
                secondsLeft = timerBar.getValue() / 10;
                lblTimerNum.setText(secondsLeft + "s");
            }

            if (timerBar.getValue() <= 0) {
                speedTimer.stop();
                handleGameOver();
            }
        });
        speedTimer.start();
    }

    private void nextQuestion() {
        Collections.shuffle(signPool);
        currentSign = signPool.get(0);
        gestureRenderer.setSign(currentSign);

        List<String> choices = new ArrayList<>();
        choices.add(currentSign.getSymbol());

        for (int i = 1; i < signPool.size() && choices.size() < 3; i++) {
            String sym = signPool.get(i).getSymbol();
            if (!choices.contains(sym)) {
                choices.add(sym);
            }
        }
        Collections.shuffle(choices);

        for (int i = 0; i < 3; i++) {
            optionButtons[i].setText(choices.get(i));
        }
    }

    private void handleOptionClicked(int index) {
        String picked = optionButtons[index].getText();
        if (picked.equals(currentSign.getSymbol())) {
            streak++;
            correctCount++;
            int points = 10 + (streak * 2);
            score += points;
            lblScore.setText("Score: " + score);
            lblStreak.setText("Streak: " + streak);
        } else {
            streak = 0;
            lblStreak.setText("Streak: 0");
        }
        nextQuestion();
    }

    private void handleGameOver() {
        int userId = AuthService.getInstance().getCurrentUser().getId();
        int finalScore = gameService.finishSpeedGame(userId, score, correctCount, 30);

        StringBuilder sb = new StringBuilder();
        sb.append("TIME IS UP!\n\n");
        sb.append("Final Score: ").append(finalScore).append(" points\n");
        sb.append("Signs Identified: ").append(correctCount).append("\n");
        sb.append("XP Earned: +").append(finalScore / 3).append(" XP\n\n");
        sb.append("Score has been recorded to your profile!");

        JOptionPane.showMessageDialog(this, sb.toString(), "Speed Blitz Completed", JOptionPane.INFORMATION_MESSAGE);
        cardLayout.show(container, "START");
    }
}
