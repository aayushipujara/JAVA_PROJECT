package com.signbridge.ui.components;

import com.signbridge.model.SignItem;
import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SignCard extends JPanel {
    private final SignItem sign;
    private boolean isFlipped = false;
    private final JPanel frontPanel;
    private final JPanel backPanel;
    private final CardLayout cardLayout;

    public SignCard(SignItem sign) {
        this.sign = sign;
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setPreferredSize(new Dimension(170, 220));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Front view (Gesture Graphic + Symbol)
        frontPanel = new JPanel(new BorderLayout(6, 6));
        frontPanel.setBackground(UITheme.CARD_BG);
        frontPanel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel catBadge = UITheme.createBadge(sign.getCategory().toUpperCase(),
                "Alphabet".equalsIgnoreCase(sign.getCategory()) ? UITheme.PRIMARY :
                "Number".equalsIgnoreCase(sign.getCategory()) ? UITheme.CYAN : UITheme.PURPLE);
        frontPanel.add(catBadge, BorderLayout.NORTH);

        GestureRenderer renderer = new GestureRenderer(sign, 130, 120);
        frontPanel.add(renderer, BorderLayout.CENTER);

        JLabel lblSymbol = new JLabel(sign.getSymbol(), SwingConstants.CENTER);
        lblSymbol.setFont(UITheme.FONT_SUBTITLE);
        lblSymbol.setForeground(UITheme.TEXT_LIGHT);
        frontPanel.add(lblSymbol, BorderLayout.SOUTH);

        // Back view (Meaning + Finger Instructions)
        backPanel = new JPanel(new BorderLayout(8, 8));
        backPanel.setBackground(UITheme.SURFACE);
        backPanel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.PRIMARY, 1, true),
                new EmptyBorder(12, 10, 12, 10)
        ));

        JLabel lblTitle = new JLabel("Meaning: " + sign.getSymbol(), SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_BOLD);
        lblTitle.setForeground(UITheme.PRIMARY);
        backPanel.add(lblTitle, BorderLayout.NORTH);

        JTextArea txtDesc = new JTextArea(sign.getDescription());
        txtDesc.setFont(UITheme.FONT_SMALL);
        txtDesc.setForeground(UITheme.TEXT_LIGHT);
        txtDesc.setBackground(UITheme.SURFACE);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setEditable(false);
        txtDesc.setFocusable(false);
        backPanel.add(txtDesc, BorderLayout.CENTER);

        JLabel lblTip = new JLabel(" " + (sign.getHint() != null ? sign.getHint() : "Click to flip"), SwingConstants.CENTER);
        lblTip.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblTip.setForeground(UITheme.WARNING);
        backPanel.add(lblTip, BorderLayout.SOUTH);

        add(frontPanel, "FRONT");
        add(backPanel, "BACK");

        // Click to flip
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                flip();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                frontPanel.setBackground(UITheme.CARD_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                frontPanel.setBackground(UITheme.CARD_BG);
            }
        });
    }

    public void flip() {
        isFlipped = !isFlipped;
        cardLayout.show(this, isFlipped ? "BACK" : "FRONT");
    }

    public SignItem getSign() {
        return sign;
    }
}
