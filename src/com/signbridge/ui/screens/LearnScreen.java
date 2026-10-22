package com.signbridge.ui.screens;

import com.signbridge.db.dao.SignDAO;
import com.signbridge.model.SignItem;
import com.signbridge.ui.MainFrame;
import com.signbridge.ui.components.GestureRenderer;
import com.signbridge.ui.components.SignCard;
import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LearnScreen extends JPanel {
    private final MainFrame mainFrame;
    private final SignDAO signDAO;
    private List<SignItem> allSigns;
    private List<SignItem> displayedSigns;

    private final JPanel gridContainer;
    private final JTextField txtSearch;
    private String currentCategory = "All";

    private final CardLayout modeLayout;
    private final JPanel modeContainer;
    private int flashcardIndex = 0;
    private final GestureRenderer flashRenderer;
    private final JLabel lblFlashSymbol;
    private final JLabel lblFlashCategory;
    private final JTextArea txtFlashDesc;
    private final JLabel lblFlashHint;
    private final JLabel lblFlashCounter;
    private boolean flashcardFlipped = false;
    private final JPanel flashFront;
    private final JPanel flashBack;
    private final CardLayout flashCardLayout;

    public LearnScreen(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.signDAO = new SignDAO();
        this.allSigns = new ArrayList<>();
        this.displayedSigns = new ArrayList<>();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 24, 16, 24));

        modeLayout = new CardLayout();
        modeContainer = new JPanel(modeLayout);
        modeContainer.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setOpaque(false);

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilters.setOpaque(false);

        JButton btnBackDash = UITheme.createSecondaryButton("< Dashboard");
        btnBackDash.addActionListener(e -> mainFrame.showScreen(MainFrame.SCREEN_DASHBOARD));
        leftFilters.add(btnBackDash);

        String[] cats = {"All", "Alphabet", "Number", "Word"};
        String[] labels = {"All Signs", "Alphabets (A-Z)", "Numbers (0-9)", "Everyday Words"};
        for (int i = 0; i < cats.length; i++) {
            final String cat = cats[i];
            JButton btnCat = UITheme.createSecondaryButton(labels[i]);
            btnCat.addActionListener(e -> {
                currentCategory = cat;
                filterSigns();
            });
            leftFilters.add(btnCat);
        }
        topBar.add(leftFilters, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_MUTED);
        rightControls.add(lblSearch);

        txtSearch = new JTextField(12);
        txtSearch.setFont(UITheme.FONT_BODY);
        txtSearch.setBackground(UITheme.SURFACE);
        txtSearch.setForeground(UITheme.TEXT_LIGHT);
        txtSearch.setCaretColor(Color.WHITE);
        txtSearch.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true), new EmptyBorder(6, 10, 6, 10)));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterSigns();
            }
        });
        rightControls.add(txtSearch);

        JButton btnToggleMode = UITheme.createPrimaryButton("Practice Flashcards");
        btnToggleMode.addActionListener(e -> {
            if ("GRID".equals(btnToggleMode.getClientProperty("mode"))) {
                btnToggleMode.putClientProperty("mode", "FLASH");
                btnToggleMode.setText("Switch to Grid View");
                modeLayout.show(modeContainer, "FLASH");
                updateFlashcardView();
            } else {
                btnToggleMode.putClientProperty("mode", "GRID");
                btnToggleMode.setText("Practice Flashcards");
                modeLayout.show(modeContainer, "GRID");
            }
        });
        btnToggleMode.putClientProperty("mode", "GRID");
        rightControls.add(btnToggleMode);

        topBar.add(rightControls, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Grid Mode
        gridContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        gridContainer.setBackground(UITheme.BG_DARK);

        JScrollPane scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(UITheme.BG_DARK);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK);
        modeContainer.add(scrollPane, "GRID");

        // Flashcard Mode
        JPanel flashcardCenter = new JPanel(new GridBagLayout());
        flashcardCenter.setOpaque(false);

        JPanel flashcardBox = new JPanel(new BorderLayout(14, 14));
        flashcardBox.setPreferredSize(new Dimension(460, 480));
        flashcardBox.setOpaque(false);

        JPanel flashTop = new JPanel(new BorderLayout());
        flashTop.setOpaque(false);
        lblFlashCategory = UITheme.createBadge("ALPHABET", UITheme.PRIMARY);
        lblFlashCounter = new JLabel("1 / 1", SwingConstants.RIGHT);
        lblFlashCounter.setFont(UITheme.FONT_BOLD);
        lblFlashCounter.setForeground(UITheme.TEXT_MUTED);
        flashTop.add(lblFlashCategory, BorderLayout.WEST);
        flashTop.add(lblFlashCounter, BorderLayout.EAST);
        flashcardBox.add(flashTop, BorderLayout.NORTH);

        flashCardLayout = new CardLayout();
        JPanel flashCardInner = new JPanel(flashCardLayout);
        flashCardInner.setOpaque(false);
        flashCardInner.setCursor(new Cursor(Cursor.HAND_CURSOR));

        flashFront = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UITheme.PRIMARY);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        flashFront.setOpaque(false);
        flashFront.setBorder(new EmptyBorder(16, 16, 16, 16));

        flashRenderer = new GestureRenderer(null, 220, 200);
        flashFront.add(flashRenderer, BorderLayout.CENTER);

        lblFlashSymbol = new JLabel("A", SwingConstants.CENTER);
        lblFlashSymbol.setFont(UITheme.FONT_BIG_NUMBER);
        lblFlashSymbol.setForeground(UITheme.TEXT_LIGHT);
        flashFront.add(lblFlashSymbol, BorderLayout.SOUTH);

        flashBack = new JPanel(new BorderLayout(12, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UITheme.CYAN);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        flashBack.setOpaque(false);
        flashBack.setBorder(new EmptyBorder(24, 20, 24, 20));

        JLabel lblBackTitle = new JLabel("How to Form This Sign", SwingConstants.CENTER);
        lblBackTitle.setFont(UITheme.FONT_SUBTITLE);
        lblBackTitle.setForeground(UITheme.CYAN);
        flashBack.add(lblBackTitle, BorderLayout.NORTH);

        txtFlashDesc = new JTextArea();
        txtFlashDesc.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtFlashDesc.setForeground(UITheme.TEXT_LIGHT);
        txtFlashDesc.setBackground(UITheme.SURFACE);
        txtFlashDesc.setLineWrap(true);
        txtFlashDesc.setWrapStyleWord(true);
        txtFlashDesc.setEditable(false);
        flashBack.add(txtFlashDesc, BorderLayout.CENTER);

        lblFlashHint = new JLabel("Click to flip back", SwingConstants.CENTER);
        lblFlashHint.setFont(UITheme.FONT_BOLD);
        lblFlashHint.setForeground(UITheme.WARNING);
        flashBack.add(lblFlashHint, BorderLayout.SOUTH);

        flashCardInner.add(flashFront, "FRONT");
        flashCardInner.add(flashBack, "BACK");
        flashCardInner.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                toggleFlashcardFlip();
            }
        });
        flashcardBox.add(flashCardInner, BorderLayout.CENTER);

        JPanel flashControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        flashControls.setOpaque(false);

        JButton btnPrev = UITheme.createSecondaryButton("Previous");
        btnPrev.addActionListener(e -> prevFlashcard());

        JButton btnFlip = UITheme.createPrimaryButton("Flip Card");
        btnFlip.addActionListener(e -> toggleFlashcardFlip());

        JButton btnNext = UITheme.createSecondaryButton("Next");
        btnNext.addActionListener(e -> nextFlashcard());

        JButton btnShuffle = UITheme.createSecondaryButton("Shuffle");
        btnShuffle.addActionListener(e -> {
            Collections.shuffle(displayedSigns);
            flashcardIndex = 0;
            updateFlashcardView();
        });

        flashControls.add(btnPrev);
        flashControls.add(btnFlip);
        flashControls.add(btnNext);
        flashControls.add(btnShuffle);
        flashcardBox.add(flashControls, BorderLayout.SOUTH);

        flashcardCenter.add(flashcardBox);
        modeContainer.add(flashcardCenter, "FLASH");

        add(modeContainer, BorderLayout.CENTER);
    }

    public void refreshData() {
        allSigns = signDAO.getAllSigns();
        filterSigns();
    }

    private void filterSigns() {
        String query = txtSearch.getText().trim().toLowerCase();
        displayedSigns.clear();

        for (SignItem s : allSigns) {
            boolean matchesCat = currentCategory.equals("All") || s.getCategory().equalsIgnoreCase(currentCategory);
            boolean matchesQuery = query.isEmpty() ||
                    s.getSymbol().toLowerCase().contains(query) ||
                    s.getDescription().toLowerCase().contains(query);

            if (matchesCat && matchesQuery) {
                displayedSigns.add(s);
            }
        }

        gridContainer.removeAll();
        for (SignItem item : displayedSigns) {
            gridContainer.add(new SignCard(item));
        }
        gridContainer.revalidate();
        gridContainer.repaint();

        if (flashcardIndex >= displayedSigns.size()) {
            flashcardIndex = 0;
        }
        updateFlashcardView();
    }

    private void toggleFlashcardFlip() {
        flashcardFlipped = !flashcardFlipped;
        flashCardLayout.show(flashFront.getParent(), flashcardFlipped ? "BACK" : "FRONT");
    }

    private void nextFlashcard() {
        if (!displayedSigns.isEmpty()) {
            flashcardIndex = (flashcardIndex + 1) % displayedSigns.size();
            flashcardFlipped = false;
            flashCardLayout.show(flashFront.getParent(), "FRONT");
            updateFlashcardView();
        }
    }

    private void prevFlashcard() {
        if (!displayedSigns.isEmpty()) {
            flashcardIndex = (flashcardIndex - 1 + displayedSigns.size()) % displayedSigns.size();
            flashcardFlipped = false;
            flashCardLayout.show(flashFront.getParent(), "FRONT");
            updateFlashcardView();
        }
    }

    private void updateFlashcardView() {
        if (displayedSigns.isEmpty()) {
            lblFlashCounter.setText("0 / 0");
            lblFlashSymbol.setText("-");
            txtFlashDesc.setText("No signs match your search or filter.");
            return;
        }

        SignItem cur = displayedSigns.get(flashcardIndex);
        lblFlashCounter.setText((flashcardIndex + 1) + " / " + displayedSigns.size());
        lblFlashCategory.setText(cur.getCategory().toUpperCase());
        lblFlashSymbol.setText(cur.getSymbol());
        txtFlashDesc.setText(cur.getDescription());
        lblFlashHint.setText(cur.getHint() != null ? cur.getHint() : "Click to flip card");
        flashRenderer.setSign(cur);
    }
}
