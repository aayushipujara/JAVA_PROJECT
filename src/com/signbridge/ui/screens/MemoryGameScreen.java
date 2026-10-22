package com.signbridge.ui.screens;

import com.signbridge.model.SignItem;
import com.signbridge.service.AuthService;
import com.signbridge.service.GameService;
import com.signbridge.ui.MainFrame;
import com.signbridge.ui.components.GestureRenderer;
import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MemoryGameScreen extends JPanel {
    private final MainFrame mainFrame;
    private final GameService gameService;

    private List<GameService.MemoryCard> cards;
    private final JPanel cardsGrid;
    private final JLabel lblMoves;
    private final JLabel lblTimer;
    private final JLabel lblPairs;

    private int moves = 0;
    private int pairsMatched = 0;
    private int secondsElapsed = 0;
    private Timer gameTimer;
    private boolean isBusy = false;

    private GameService.MemoryCard firstSelected = null;
    private JPanel firstCardPanel = null;

    public MemoryGameScreen(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.gameService = new GameService();

        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 28, 16, 28));

        // Header: Title, Moves, Timer, Dashboard button
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        JButton btnBack = UITheme.createSecondaryButton("< Dashboard");
        btnBack.addActionListener(e -> {
            stopTimer();
            mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD);
        });
        leftPanel.add(btnBack);

        JLabel title = new JLabel("Memory Match Arcade");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.WARNING);
        leftPanel.add(title);
        topBar.add(leftPanel, BorderLayout.WEST);

        // Stats ticker (Moves, Pairs, Time)
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        statsPanel.setOpaque(false);

        lblPairs = new JLabel("Pairs: 0 / 6");
        lblPairs.setFont(UITheme.FONT_BOLD);
        lblPairs.setForeground(UITheme.TEXT_LIGHT);

        lblMoves = new JLabel("Moves: 0");
        lblMoves.setFont(UITheme.FONT_BOLD);
        lblMoves.setForeground(UITheme.CYAN);

        lblTimer = new JLabel("Time: 0s");
        lblTimer.setFont(UITheme.FONT_BOLD);
        lblTimer.setForeground(UITheme.SUCCESS);

        JButton btnRestart = UITheme.createSecondaryButton("New Game");
        btnRestart.addActionListener(e -> startNewGame());

        statsPanel.add(lblPairs);
        statsPanel.add(lblMoves);
        statsPanel.add(lblTimer);
        statsPanel.add(btnRestart);
        topBar.add(statsPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center 4x3 Cards Grid
        cardsGrid = new JPanel(new GridLayout(3, 4, 12, 12));
        cardsGrid.setBackground(UITheme.BG_DARK);
        add(cardsGrid, BorderLayout.CENTER);
    }

    public void startNewGame() {
        stopTimer();
        moves = 0;
        pairsMatched = 0;
        secondsElapsed = 0;
        firstSelected = null;
        firstCardPanel = null;
        isBusy = false;

        lblMoves.setText("Moves: 0");
        lblPairs.setText("Pairs: 0 / 6");
        lblTimer.setText("Time: 0s");

        cards = gameService.setupMemoryGame(6); // 6 pairs = 12 cards (4x3 grid)
        renderCards();

        gameTimer = new Timer(1000, e -> {
            secondsElapsed++;
            lblTimer.setText("Time: " + secondsElapsed + "s");
        });
        gameTimer.start();
    }

    private void stopTimer() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    private void renderCards() {
        cardsGrid.removeAll();
        for (GameService.MemoryCard card : cards) {
            JPanel cardView = createCardView(card);
            cardsGrid.add(cardView);
        }
        cardsGrid.revalidate();
        cardsGrid.repaint();
    }

    private JPanel createCardView(GameService.MemoryCard card) {
        CardLayout cl = new CardLayout();
        JPanel panel = new JPanel(cl);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // BACK of card (Hidden state)
        JPanel backView = new JPanel(new BorderLayout());
        backView.setBackground(UITheme.CARD_BG);
        backView.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true), new EmptyBorder(10, 10, 10, 10)));
        JLabel lblCover = new JLabel("", SwingConstants.CENTER);
        lblCover.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblCover.setForeground(UITheme.PRIMARY);
        backView.add(lblCover, BorderLayout.CENTER);

        // FRONT of card (Revealed state)
        JPanel frontView = new JPanel(new BorderLayout(4, 4));
        frontView.setBackground(UITheme.SURFACE);
        frontView.setBorder(new CompoundBorder(new LineBorder(UITheme.WARNING, 2, true), new EmptyBorder(8, 8, 8, 8)));

        if (card.isGesture) {
            GestureRenderer renderer = new GestureRenderer(card.sign, 110, 95);
            frontView.add(renderer, BorderLayout.CENTER);
            JLabel lblHint = new JLabel("Hand Gesture", SwingConstants.CENTER);
            lblHint.setFont(UITheme.FONT_SMALL);
            lblHint.setForeground(UITheme.TEXT_MUTED);
            frontView.add(lblHint, BorderLayout.SOUTH);
        } else {
            JLabel lblWord = new JLabel(card.sign.getSymbol(), SwingConstants.CENTER);
            lblWord.setFont(UITheme.FONT_TITLE);
            lblWord.setForeground(UITheme.TEXT_LIGHT);
            frontView.add(lblWord, BorderLayout.CENTER);
            JLabel lblCat = new JLabel(card.sign.getCategory(), SwingConstants.CENTER);
            lblCat.setFont(UITheme.FONT_SMALL);
            lblCat.setForeground(UITheme.CYAN);
            frontView.add(lblCat, BorderLayout.SOUTH);
        }

        panel.add(backView, "BACK");
        panel.add(frontView, "FRONT");
        cl.show(panel, card.isRevealed ? "FRONT" : "BACK");

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isBusy || card.isRevealed || card.isMatched) return;
                handleCardClick(card, panel, cl);
            }
        });

        return panel;
    }

    private void handleCardClick(GameService.MemoryCard card, JPanel panel, CardLayout cl) {
        card.isRevealed = true;
        cl.show(panel, "FRONT");

        if (firstSelected == null) {
            // First card flipped
            firstSelected = card;
            firstCardPanel = panel;
        } else {
            // Second card flipped
            moves++;
            lblMoves.setText("Moves: " + moves);

            if (firstSelected.sign.getId() == card.sign.getId() && firstSelected.isGesture != card.isGesture) {
                // Match Found!
                card.isMatched = true;
                firstSelected.isMatched = true;
                pairsMatched++;
                lblPairs.setText("Pairs: " + pairsMatched + " / 6");

                panel.setBorder(new CompoundBorder(new LineBorder(UITheme.SUCCESS, 2, true), new EmptyBorder(8, 8, 8, 8)));
                firstCardPanel.setBorder(new CompoundBorder(new LineBorder(UITheme.SUCCESS, 2, true), new EmptyBorder(8, 8, 8, 8)));

                firstSelected = null;
                firstCardPanel = null;

                if (pairsMatched == 6) {
                    handleVictory();
                }
            } else {
                // Not a match - flip back after 700ms
                isBusy = true;
                final GameService.MemoryCard prevCard = firstSelected;
                final JPanel prevPanel = firstCardPanel;

                firstSelected = null;
                firstCardPanel = null;

                Timer flipBackTimer = new Timer(700, evt -> {
                    prevCard.isRevealed = false;
                    card.isRevealed = false;
                    ((CardLayout) prevPanel.getLayout()).show(prevPanel, "BACK");
                    cl.show(panel, "BACK");
                    isBusy = false;
                });
                flipBackTimer.setRepeats(false);
                flipBackTimer.start();
            }
        }
    }

    private void handleVictory() {
        stopTimer();
        int userId = AuthService.getInstance().getCurrentUser().getId();
        int finalScore = gameService.finishMemoryGame(userId, moves, secondsElapsed);

        StringBuilder sb = new StringBuilder();
        sb.append("Congratulations! All 6 sign pairs matched!\n\n");
        sb.append("Moves: ").append(moves).append("\n");
        sb.append("Time: ").append(secondsElapsed).append(" seconds\n");
        sb.append("Final Score: ").append(finalScore).append(" pts\n");
        sb.append("XP Earned: +").append(finalScore / 5).append(" XP");

        JOptionPane.showMessageDialog(this, sb.toString(), "Victory!", JOptionPane.INFORMATION_MESSAGE);
    }
}
