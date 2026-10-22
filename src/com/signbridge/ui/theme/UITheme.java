package com.signbridge.ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UITheme {
    // Dark modern slate palette
    public static final Color BG_DARK = new Color(15, 23, 42);         // Slate 900
    public static final Color CARD_BG = new Color(30, 41, 59);         // Slate 800
    public static final Color CARD_HOVER = new Color(45, 59, 83);      // Slate 700
    public static final Color SURFACE = new Color(51, 65, 85);         // Slate 650
    public static final Color BORDER_COLOR = new Color(71, 85, 105);   // Slate 600

    // Accents
    public static final Color PRIMARY = new Color(99, 102, 241);       // Indigo 500
    public static final Color PRIMARY_HOVER = new Color(79, 70, 229);  // Indigo 600
    public static final Color SUCCESS = new Color(16, 185, 129);       // Emerald 500
    public static final Color DANGER = new Color(239, 68, 68);         // Rose 500
    public static final Color WARNING = new Color(245, 158, 11);       // Amber 500
    public static final Color CYAN = new Color(6, 182, 212);           // Cyan 500
    public static final Color PURPLE = new Color(168, 85, 247);        // Purple 500

    // Text
    public static final Color TEXT_LIGHT = new Color(248, 250, 252);   // Slate 50
    public static final Color TEXT_MUTED = new Color(148, 163, 184);   // Slate 400

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BIG_NUMBER = new Font("Segoe UI", Font.BOLD, 36);

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(PRIMARY_HOVER); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(PRIMARY); }
        });
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_LIGHT);
        btn.setBackground(SURFACE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(BORDER_COLOR); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(SURFACE); }
        });
        return btn;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    public static JLabel createBadge(String text, Color bg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        return badge;
    }

    public static void drawVectorIcon(Graphics2D g2, String iconType, int x, int y, int size, Color color) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if ("STAR".equals(iconType)) {
            int midX = x + size / 2;
            int midY = y + size / 2;
            int outerR = size / 2;
            int innerR = outerR / 2;
            java.awt.geom.GeneralPath p = new java.awt.geom.GeneralPath();
            for (int i = 0; i < 10; i++) {
                double r = (i % 2 == 0) ? outerR : innerR;
                double angle = i * Math.PI / 5 - Math.PI / 2;
                double px = midX + r * Math.cos(angle);
                double py = midY + r * Math.sin(angle);
                if (i == 0) p.moveTo(px, py);
                else p.lineTo(px, py);
            }
            p.closePath();
            g.fill(p);

        } else if ("LIGHTNING".equals(iconType)) {
            java.awt.geom.GeneralPath p = new java.awt.geom.GeneralPath();
            p.moveTo(x + size * 0.55, y);
            p.lineTo(x + size * 0.2, y + size * 0.55);
            p.lineTo(x + size * 0.5, y + size * 0.55);
            p.lineTo(x + size * 0.45, y + size);
            p.lineTo(x + size * 0.85, y + size * 0.45);
            p.lineTo(x + size * 0.55, y + size * 0.45);
            p.closePath();
            g.fill(p);

        } else if ("MEDAL".equals(iconType)) {
            int circleR = size * 3 / 5;
            g.fillOval(x + (size - circleR) / 2, y + 2, circleR, circleR);
            g.setColor(color.darker());
            g.drawOval(x + (size - circleR) / 2, y + 2, circleR, circleR);
            g.setColor(color);
            java.awt.geom.GeneralPath r1 = new java.awt.geom.GeneralPath();
            r1.moveTo(x + size / 2 - 4, y + circleR);
            r1.lineTo(x + 2, y + size);
            r1.lineTo(x + size / 2 - 2, y + size - 4);
            r1.closePath();
            g.fill(r1);

            java.awt.geom.GeneralPath r2 = new java.awt.geom.GeneralPath();
            r2.moveTo(x + size / 2 + 4, y + circleR);
            r2.lineTo(x + size - 2, y + size);
            r2.lineTo(x + size / 2 + 2, y + size - 4);
            r2.closePath();
            g.fill(r2);

        } else if ("BRAIN".equals(iconType)) {
            g.drawOval(x + 2, y + 4, size / 2, size / 2);
            g.drawOval(x + size / 2 - 2, y + 4, size / 2, size / 2);
            g.drawOval(x + 4, y + size / 2 - 2, size * 3 / 5, size / 2);
            g.fillOval(x + size / 2 - 2, y + size / 2 - 2, 6, 6);

        } else if ("CROWN".equals(iconType) || "TROPHY".equals(iconType)) {
            java.awt.geom.GeneralPath p = new java.awt.geom.GeneralPath();
            p.moveTo(x + 2, y + size * 0.25);
            p.lineTo(x + size * 0.25, y + size * 0.6);
            p.lineTo(x + size / 2, y + size * 0.15);
            p.lineTo(x + size * 0.75, y + size * 0.6);
            p.lineTo(x + size - 2, y + size * 0.25);
            p.lineTo(x + size * 0.85, y + size * 0.85);
            p.lineTo(x + size * 0.15, y + size * 0.85);
            p.closePath();
            g.fill(p);

        } else {
            java.awt.geom.GeneralPath p = new java.awt.geom.GeneralPath();
            p.moveTo(x + size / 2, y + 2);
            p.lineTo(x + size - 2, y + size / 2);
            p.lineTo(x + size / 2, y + size - 2);
            p.lineTo(x + 2, y + size / 2);
            p.closePath();
            g.fill(p);
        }
        g.dispose();
    }
}
