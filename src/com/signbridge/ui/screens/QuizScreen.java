package com.signbridge.ui.screens;

import com.signbridge.model.QuizQuestion;
import com.signbridge.model.SignItem;
import com.signbridge.service.AuthService;
import com.signbridge.service.QuizService;
import com.signbridge.ui.MainFrame;
import com.signbridge.ui.components.GestureRenderer;
import com.signbridge.ui.components.TimerBar;
import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class QuizScreen extends JPanel {
    private final MainFrame mainFrame;
    private final QuizService quizService;

    // Quiz Session State
    private List<QuizQuestion> currentQuiz;
    private int questionIndex = 0;
    private int correctCount = 0;
    private int totalTimeTaken = 0;
    private String selectedCategory = "All";

    // Question Timer (15s per question)
    private Timer questionTimer;
    private int timeLeftMillis = 15000;
    private static final int TIMER_INTERVAL = 100;

    // UI Components
    private final CardLayout screenCardLayout;
    private final JPanel container;

    // Category Select Panel
    private final JPanel selectPanel;

    // Active Question Panel
    private final JPanel quizActivePanel;
    private final JLabel lblQuestionCounter;
    private final JLabel lblScoreLive;
    private final TimerBar timerBar;
    private final GestureRenderer gestureRenderer;
    private final JLabel lblPrompt;
    private final JButton[] optionButtons = new JButton[4];
    private final JPanel explanationBox;
    private final JLabel lblExplanationText;
    private final JButton btnNext;

    // Results Panel
    private final JPanel resultsPanel;
    private final JLabel lblResultTitle;
    private final JLabel lblResultScore;
    private final JLabel lblResultAccuracy;
    private final JLabel lblResultXp;

    public QuizScreen(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.quizService = new QuizService();

        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);

        screenCardLayout = new CardLayout();
        container = new JPanel(screenCardLayout);
        container.setOpaque(false);

        // ==========================================
        // 1. SELECT CATEGORY PANEL
        // ==========================================
        selectPanel = new JPanel(new GridBagLayout());
        selectPanel.setBackground(UITheme.BG_DARK);

        JPanel selectBox = new JPanel(new BorderLayout(16, 20));
        selectBox.setBackground(UITheme.CARD_BG);
        selectBox.setPreferredSize(new Dimension(460, 420));
        selectBox.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true), new EmptyBorder(24, 28, 24, 28)));

        JPanel selHeader = new JPanel(new GridLayout(2, 1, 4, 4));
        selHeader.setOpaque(false);
        JLabel selTitle = new JLabel("SignBridge Quiz Arena", SwingConstants.CENTER);
        selTitle.setFont(UITheme.FONT_TITLE);
        selTitle.setForeground(UITheme.PRIMARY);

        JLabel selSub = new JLabel("Choose a category to test your sign skills:", SwingConstants.CENTER);
        selSub.setFont(UITheme.FONT_BODY);
        selSub.setForeground(UITheme.TEXT_MUTED);

        selHeader.add(selTitle);
        selHeader.add(selSub);
        selectBox.add(selHeader, BorderLayout.NORTH);

        JPanel catGrid = new JPanel(new GridLayout(4, 1, 10, 10));
        catGrid.setOpaque(false);

        String[] categories = {"All Signs Challenge (Mixed)", "Alphabets (A-Z)", "Numbers (0-9)", "Common Everyday Words"};
        String[] catKeys = {"All", "Alphabet", "Number", "Word"};

        for (int i = 0; i < categories.length; i++) {
            final String key = catKeys[i];
            JButton btn = UITheme.createSecondaryButton(categories[i]);
            btn.setFont(UITheme.FONT_BOLD);
            btn.addActionListener(e -> startQuiz(key));
            catGrid.add(btn);
        }
        selectBox.add(catGrid, BorderLayout.CENTER);

        JButton btnBackDash = UITheme.createSecondaryButton("< Back to Dashboard");
        btnBackDash.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD));
        selectBox.add(btnBackDash, BorderLayout.SOUTH);

        selectPanel.add(selectBox);
        container.add(selectPanel, "SELECT");

        // ==========================================
        // 2. ACTIVE QUIZ QUESTION PANEL
        // ==========================================
        quizActivePanel = new JPanel(new BorderLayout(12, 12));
        quizActivePanel.setBackground(UITheme.BG_DARK);
        quizActivePanel.setBorder(new EmptyBorder(16, 32, 16, 32));

        // Question Header: Count, TimerBar, Live Score
        JPanel qHeader = new JPanel(new BorderLayout(8, 8));
        qHeader.setOpaque(false);

        JPanel countAndScore = new JPanel(new BorderLayout());
        countAndScore.setOpaque(false);
        lblQuestionCounter = new JLabel("Question 1 of 10");
        lblQuestionCounter.setFont(UITheme.FONT_BOLD);
        lblQuestionCounter.setForeground(UITheme.PRIMARY);

        lblScoreLive = new JLabel("Score: 0", SwingConstants.RIGHT);
        lblScoreLive.setFont(UITheme.FONT_BOLD);
        lblScoreLive.setForeground(UITheme.SUCCESS);

        countAndScore.add(lblQuestionCounter, BorderLayout.WEST);
        countAndScore.add(lblScoreLive, BorderLayout.EAST);
        qHeader.add(countAndScore, BorderLayout.NORTH);

        timerBar = new TimerBar(15);
        qHeader.add(timerBar, BorderLayout.SOUTH);
        quizActivePanel.add(qHeader, BorderLayout.NORTH);

        // Center: Gesture Display & 4 Options
        JPanel qCenter = new JPanel(new BorderLayout(16, 16));
        qCenter.setOpaque(false);

        // Gesture Box + Prompt
        JPanel visualBox = new JPanel(new BorderLayout(8, 8));
        visualBox.setOpaque(false);

        lblPrompt = new JLabel("What does this hand gesture represent", SwingConstants.CENTER);
        lblPrompt.setFont(UITheme.FONT_SUBTITLE);
        lblPrompt.setForeground(UITheme.TEXT_LIGHT);
        visualBox.add(lblPrompt, BorderLayout.NORTH);

        JPanel renderWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        renderWrap.setOpaque(false);
        gestureRenderer = new GestureRenderer(null, 200, 170);
        renderWrap.add(gestureRenderer);
        visualBox.add(renderWrap, BorderLayout.CENTER);

        qCenter.add(visualBox, BorderLayout.NORTH);

        // Options 2x2 Grid
        JPanel optionsGrid = new JPanel(new GridLayout(2, 2, 14, 14));
        optionsGrid.setOpaque(false);

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            optionButtons[i] = new JButton("Option");
            optionButtons[i].setFont(new Font("Segoe UI", Font.BOLD, 18));
            optionButtons[i].setForeground(UITheme.TEXT_LIGHT);
            optionButtons[i].setBackground(UITheme.CARD_BG);
            optionButtons[i].setFocusPainted(false);
            optionButtons[i].setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true), new EmptyBorder(12, 16, 12, 16)));
            optionButtons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            optionButtons[i].addActionListener(e -> handleOptionSelected(idx));
            optionsGrid.add(optionButtons[i]);
        }
        qCenter.add(optionsGrid, BorderLayout.CENTER);
        quizActivePanel.add(qCenter, BorderLayout.CENTER);

        // Footer: Explanation & Next Button
        JPanel qFooter = new JPanel(new BorderLayout(10, 10));
        qFooter.setOpaque(false);

        explanationBox = new JPanel(new BorderLayout());
        explanationBox.setBackground(UITheme.SURFACE);
        explanationBox.setBorder(new CompoundBorder(new LineBorder(UITheme.PRIMARY, 1, true), new EmptyBorder(10, 14, 10, 14)));
        lblExplanationText = new JLabel("Explanation...");
        lblExplanationText.setFont(UITheme.FONT_BODY);
        lblExplanationText.setForeground(UITheme.TEXT_LIGHT);
        explanationBox.add(lblExplanationText, BorderLayout.CENTER);
        explanationBox.setVisible(false);
        qFooter.add(explanationBox, BorderLayout.CENTER);

        btnNext = UITheme.createPrimaryButton("Next Question >");
        btnNext.setVisible(false);
        btnNext.addActionListener(e -> nextQuestion());
        qFooter.add(btnNext, BorderLayout.EAST);

        quizActivePanel.add(qFooter, BorderLayout.SOUTH);
        container.add(quizActivePanel, "QUIZ");

        // ==========================================
        // 3. RESULTS SUMMARY PANEL
        // ==========================================
        resultsPanel = new JPanel(new GridBagLayout());
        resultsPanel.setBackground(UITheme.BG_DARK);

        JPanel resBox = new JPanel(new BorderLayout(16, 20));
        resBox.setBackground(UITheme.CARD_BG);
        resBox.setPreferredSize(new Dimension(460, 440));
        resBox.setBorder(new CompoundBorder(new LineBorder(UITheme.SUCCESS, 2, true), new EmptyBorder(24, 28, 24, 28)));

        lblResultTitle = new JLabel("Quiz Completed!", SwingConstants.CENTER);
        lblResultTitle.setFont(UITheme.FONT_TITLE);
        lblResultTitle.setForeground(UITheme.SUCCESS);
        resBox.add(lblResultTitle, BorderLayout.NORTH);

        JPanel resStats = new JPanel(new GridLayout(3, 1, 12, 12));
        resStats.setOpaque(false);

        lblResultScore = new JLabel("Score: 8 / 10", SwingConstants.CENTER);
        lblResultScore.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblResultScore.setForeground(UITheme.TEXT_LIGHT);

        lblResultAccuracy = new JLabel("Accuracy: 80%", SwingConstants.CENTER);
        lblResultAccuracy.setFont(UITheme.FONT_SUBTITLE);
        lblResultAccuracy.setForeground(UITheme.CYAN);

        lblResultXp = new JLabel("+200 XP Awarded", SwingConstants.CENTER);
        lblResultXp.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblResultXp.setForeground(UITheme.WARNING);

        resStats.add(lblResultScore);
        resStats.add(lblResultAccuracy);
        resStats.add(lblResultXp);
        resBox.add(resStats, BorderLayout.CENTER);

        JPanel resActions = new JPanel(new GridLayout(1, 2, 12, 0));
        resActions.setOpaque(false);

        JButton btnRetry = UITheme.createSecondaryButton("Retake Quiz");
        btnRetry.addActionListener(e -> startQuiz(selectedCategory));

        JButton btnDone = UITheme.createPrimaryButton("Dashboard");
        btnDone.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD));

        resActions.add(btnRetry);
        resActions.add(btnDone);
        resBox.add(resActions, BorderLayout.SOUTH);

        resultsPanel.add(resBox);
        container.add(resultsPanel, "RESULTS");

        add(container, BorderLayout.CENTER);
    }

    public void startQuiz(String category) {
        this.selectedCategory = category;
        this.currentQuiz = quizService.generateQuiz(category, 10);
        this.questionIndex = 0;
        this.correctCount = 0;
        this.totalTimeTaken = 0;

        if (currentQuiz.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No quiz questions available for this category.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        screenCardLayout.show(container, "QUIZ");
        displayQuestion();
    }

    private void displayQuestion() {
        if (questionIndex >= currentQuiz.size()) {
            finishQuiz();
            return;
        }

        QuizQuestion q = currentQuiz.get(questionIndex);
        lblQuestionCounter.setText("Question " + (questionIndex + 1) + " of " + currentQuiz.size());
        lblScoreLive.setText("Score: " + correctCount);
        lblPrompt.setText(q.getQuestionText());
        gestureRenderer.setSign(q.getTargetSign());

        List<String> opts = q.getOptions();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setEnabled(true);
            optionButtons[i].setBackground(UITheme.CARD_BG);
            optionButtons[i].setForeground(UITheme.TEXT_LIGHT);
            optionButtons[i].setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true), new EmptyBorder(12, 16, 12, 16)));
            if (i < opts.size()) {
                optionButtons[i].setText(opts.get(i));
                optionButtons[i].setVisible(true);
            } else {
                optionButtons[i].setVisible(false);
            }
        }

        explanationBox.setVisible(false);
        btnNext.setVisible(false);

        // Reset & Start 15s Timer
        timeLeftMillis = 15000;
        timerBar.setMaximum(150);
        timerBar.setValue(150);

        if (questionTimer != null && questionTimer.isRunning()) {
            questionTimer.stop();
        }

        questionTimer = new Timer(TIMER_INTERVAL, e -> {
            timeLeftMillis -= TIMER_INTERVAL;
            timerBar.setValue(timeLeftMillis / 100);
            if (timeLeftMillis <= 0) {
                questionTimer.stop();
                totalTimeTaken += 15;
                handleTimeOut();
            }
        });
        questionTimer.start();
    }

    private void handleOptionSelected(int selectedIndex) {
        if (questionTimer != null && questionTimer.isRunning()) {
            questionTimer.stop();
        }

        int elapsedSec = Math.max(1, (15000 - timeLeftMillis) / 1000);
        totalTimeTaken += elapsedSec;

        QuizQuestion q = currentQuiz.get(questionIndex);
        boolean isRight = q.isCorrect(selectedIndex);

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setEnabled(false);
        }

        if (isRight) {
            correctCount++;
            lblScoreLive.setText("Score: " + correctCount);
            optionButtons[selectedIndex].setBackground(UITheme.SUCCESS);
            lblExplanationText.setText("Correct! " + q.getExplanation());
        } else {
            optionButtons[selectedIndex].setBackground(UITheme.DANGER);
            optionButtons[q.getCorrectIndex()].setBackground(UITheme.SUCCESS);
            lblExplanationText.setText("Incorrect. " + q.getExplanation());
        }

        explanationBox.setVisible(true);
        btnNext.setVisible(true);
    }

    private void handleTimeOut() {
        QuizQuestion q = currentQuiz.get(questionIndex);
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setEnabled(false);
        }
        optionButtons[q.getCorrectIndex()].setBackground(UITheme.SUCCESS);
        lblExplanationText.setText("Time's up! " + q.getExplanation());
        explanationBox.setVisible(true);
        btnNext.setVisible(true);
    }

    private void nextQuestion() {
        questionIndex++;
        displayQuestion();
    }

    private void finishQuiz() {
        if (questionTimer != null) questionTimer.stop();

        int userId = AuthService.getInstance().getCurrentUser().getId();
        int totalQuestions = currentQuiz.size();
        int xpEarned = quizService.finishQuiz(userId, selectedCategory, correctCount, totalQuestions, totalTimeTaken);

        double acc = (correctCount * 100.0) / totalQuestions;
        lblResultScore.setText("Score: " + correctCount + " / " + totalQuestions);
        lblResultAccuracy.setText("Accuracy: " + Math.round(acc) + "% in " + totalTimeTaken + "s");
        lblResultXp.setText("+" + xpEarned + " XP Earned!");

        screenCardLayout.show(container, "RESULTS");
    }
}
