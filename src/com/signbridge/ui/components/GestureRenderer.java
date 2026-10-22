package com.signbridge.ui.components;

import com.signbridge.model.SignItem;
import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class GestureRenderer extends JPanel {
    private SignItem sign;
    private int renderWidth = 140;
    private int renderHeight = 140;

    public GestureRenderer(SignItem sign) {
        this.sign = sign;
        setPreferredSize(new Dimension(renderWidth, renderHeight));
        setOpaque(false);
    }

    public GestureRenderer(SignItem sign, int width, int height) {
        this.sign = sign;
        this.renderWidth = width;
        this.renderHeight = height;
        setPreferredSize(new Dimension(width, height));
        setOpaque(false);
    }

    public void setSign(SignItem sign) {
        this.sign = sign;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (sign == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background subtle container circle/rounded rect
        g2.setColor(new Color(15, 23, 42, 180));
        g2.fillRoundRect(4, 4, w - 8, h - 8, 20, 20);
        g2.setColor(UITheme.BORDER_COLOR);
        g2.drawRoundRect(4, 4, w - 8, h - 8, 20, 20);

        // Render gesture based on gestureType / symbol
        drawVectorHand(g2, w, h, sign);

        g2.dispose();
    }

    private void drawVectorHand(Graphics2D g2, int w, int h, SignItem sign) {
        int cx = w / 2;
        int cy = h / 2 + 10;
        String type = sign.getGestureType() != null ? sign.getGestureType() : "ALPHA_" + sign.getSymbol();

        // Skin tones
        Color skinColor = new Color(245, 195, 150);
        Color skinShadow = new Color(215, 160, 115);
        Color fingerLine = new Color(160, 110, 75);
        Color accentColor = UITheme.PRIMARY;

        // Base Palm
        RoundRectangle2D palm = new RoundRectangle2D.Double(cx - 24, cy - 18, 48, 44, 16, 16);
        g2.setColor(skinColor);
        g2.fill(palm);
        g2.setColor(fingerLine);
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(palm);

        // Wrist
        RoundRectangle2D wrist = new RoundRectangle2D.Double(cx - 16, cy + 24, 32, 22, 10, 10);
        g2.setColor(skinShadow);
        g2.fill(wrist);
        g2.setColor(fingerLine);
        g2.draw(wrist);

        // Distinct finger rendering per gesture
        drawFingersForSign(g2, cx, cy, type, sign.getSymbol(), skinColor, skinShadow, fingerLine);

        // Overlay Symbol Watermark Badge
        g2.setColor(new Color(99, 102, 241, 220));
        g2.fillRoundRect(w - 38, 8, 30, 24, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        int strW = fm.stringWidth(sign.getSymbol());
        g2.drawString(sign.getSymbol(), w - 38 + (30 - strW) / 2, 8 + 17);
    }

    private void drawFingersForSign(Graphics2D g2, int cx, int cy, String type, String sym,
                                   Color skin, Color shadow, Color outline) {
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Logic based on gesture type
        if ("ALPHA_A".equals(type) || "WORD_YES".equals(type) || "WORD_SORRY".equals(type)) {
            // Fist with thumb up resting on side
            drawFistCurled(g2, cx, cy, skin, outline);
            // Upright thumb on side
            drawFinger(g2, cx - 28, cy - 20, 12, 30, -15, skin, outline);

        } else if ("ALPHA_B".equals(type) || "NUM_4".equals(type)) {
            // 4 fingers straight up
            for (int i = 0; i < 4; i++) {
                int fx = cx - 18 + (i * 12);
                drawFinger(g2, fx, cy - 56, 11, 42, 0, skin, outline);
            }
            // Thumb folded across palm
            drawFinger(g2, cx - 16, cy + 6, 26, 12, 10, skin, outline);

        } else if ("ALPHA_C".equals(type) || "NUM_0".equals(type)) {
            // C curved shape
            g2.setColor(skin);
            Arc2D cArc = new Arc2D.Double(cx - 30, cy - 40, 56, 56, 45, 270, Arc2D.OPEN);
            g2.setStroke(new BasicStroke(16.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(cArc);
            g2.setColor(outline);
            g2.setStroke(new BasicStroke(2.2f));
            g2.draw(new Arc2D.Double(cx - 38, cy - 48, 72, 72, 45, 270, Arc2D.OPEN));
            g2.draw(new Arc2D.Double(cx - 22, cy - 32, 40, 40, 45, 270, Arc2D.OPEN));

        } else if ("ALPHA_D".equals(type) || "NUM_1".equals(type)) {
            // 1 finger straight up (index), others curled meeting thumb
            drawFinger(g2, cx - 12, cy - 58, 12, 44, 0, skin, outline);
            drawCurledKnuckle(g2, cx + 2, cy - 10, 24, 18, skin, outline);

        } else if ("ALPHA_V".equals(type) || "NUM_2".equals(type) || "WORD_PEACE".equals(type)) {
            // V shape: index & middle spread
            drawFinger(g2, cx - 18, cy - 56, 12, 42, -18, skin, outline);
            drawFinger(g2, cx + 6, cy - 56, 12, 42, 18, skin, outline);
            drawCurledKnuckle(g2, cx - 14, cy - 4, 28, 16, skin, outline);

        } else if ("ALPHA_W".equals(type) || "NUM_3".equals(type) || "WORD_WATER".equals(type)) {
            // W shape: 3 fingers spread
            drawFinger(g2, cx - 22, cy - 54, 11, 40, -20, skin, outline);
            drawFinger(g2, cx - 5, cy - 58, 11, 44, 0, skin, outline);
            drawFinger(g2, cx + 12, cy - 54, 11, 40, 20, skin, outline);
            drawCurledKnuckle(g2, cx - 12, cy + 2, 24, 14, skin, outline);

        } else if ("ALPHA_L".equals(type)) {
            // L shape: index up, thumb 90 deg sideways
            drawFinger(g2, cx - 10, cy - 58, 12, 44, 0, skin, outline);
            drawFinger(g2, cx - 44, cy - 6, 36, 12, 0, skin, outline);
            drawCurledKnuckle(g2, cx + 2, cy - 6, 20, 22, skin, outline);

        } else if ("ALPHA_I".equals(type) || "ALPHA_J".equals(type)) {
            // Pinky up, others curled
            drawFinger(g2, cx + 18, cy - 54, 10, 38, 5, skin, outline);
            drawFistCurled(g2, cx - 4, cy, skin, outline);
            if ("ALPHA_J".equals(type)) {
                // Draw J motion arrow
                drawMotionArrow(g2, cx + 24, cy - 20, cx + 10, cy + 10);
            }

        } else if ("ALPHA_Y".equals(type)) {
            // Thumb and pinky extended out (Shaka / phone call)
            drawFinger(g2, cx - 44, cy - 24, 34, 12, -25, skin, outline);
            drawFinger(g2, cx + 16, cy - 24, 34, 12, 25, skin, outline);
            drawCurledKnuckle(g2, cx - 14, cy - 10, 28, 22, skin, outline);

        } else if ("ALPHA_F".equals(type) || "NUM_9".equals(type)) {
            // OK sign: index and thumb circle, 3 fingers up
            drawFinger(g2, cx - 6, cy - 56, 11, 42, -5, skin, outline);
            drawFinger(g2, cx + 8, cy - 54, 11, 40, 10, skin, outline);
            drawFinger(g2, cx + 20, cy - 48, 10, 36, 20, skin, outline);
            // Circle between thumb and index
            g2.setColor(skin);
            g2.fillOval(cx - 30, cy - 20, 24, 24);
            g2.setColor(outline);
            g2.drawOval(cx - 30, cy - 20, 24, 24);

        } else if ("NUM_5".equals(type) || "WORD_HELLO".equals(type) || "WORD_PLEASE".equals(type)) {
            // Open 5 fingers
            drawFinger(g2, cx - 40, cy - 20, 28, 12, -35, skin, outline); // Thumb
            drawFinger(g2, cx - 22, cy - 54, 11, 40, -15, skin, outline);
            drawFinger(g2, cx - 8, cy - 58, 11, 44, 0, skin, outline);
            drawFinger(g2, cx + 6, cy - 54, 11, 40, 12, skin, outline);
            drawFinger(g2, cx + 18, cy - 46, 10, 34, 24, skin, outline);

            if ("WORD_HELLO".equals(type)) {
                drawMotionArrow(g2, cx - 20, cy - 50, cx + 30, cy - 50);
            } else if ("WORD_PLEASE".equals(type)) {
                // Circular motion indicator
                drawCircularArrow(g2, cx, cy);
            }

        } else if ("WORD_LOVE".equals(type)) {
            // Crossed arms representation with Heart
            g2.setColor(new Color(239, 68, 68));
            drawHeart(g2, cx - 18, cy - 40, 36, 32);
            drawFistCurled(g2, cx - 16, cy, skin, outline);
            drawFistCurled(g2, cx + 16, cy, skin, outline);

        } else if ("WORD_THANK_YOU".equals(type) || "WORD_HELP".equals(type)) {
            // Hand extending outward or flat hand supporting
            for (int i = 0; i < 4; i++) {
                drawFinger(g2, cx - 16 + (i * 10), cy - 48, 10, 36, 0, skin, outline);
            }
            drawFinger(g2, cx - 34, cy - 14, 24, 11, -30, skin, outline);
            drawMotionArrow(g2, cx, cy - 20, cx + 24, cy - 40);

        } else {
            // General clean stylized hand gesture representation
            drawFingersGeneric(g2, cx, cy, skin, outline);
        }
    }

    private void drawFinger(Graphics2D g2, int x, int y, int width, int height, double angleDeg, Color fill, Color outline) {
        AffineTransform old = g2.getTransform();
        g2.rotate(Math.toRadians(angleDeg), x + width / 2.0, y + height);
        RoundRectangle2D f = new RoundRectangle2D.Double(x, y, width, height, width, width);
        g2.setColor(fill);
        g2.fill(f);
        g2.setColor(outline);
        g2.draw(f);
        g2.setTransform(old);
    }

    private void drawFistCurled(Graphics2D g2, int cx, int cy, Color fill, Color outline) {
        g2.setColor(fill);
        for (int i = 0; i < 4; i++) {
            g2.fillOval(cx - 18 + (i * 10), cy - 16, 12, 14);
            g2.setColor(outline);
            g2.drawOval(cx - 18 + (i * 10), cy - 16, 12, 14);
            g2.setColor(fill);
        }
    }

    private void drawCurledKnuckle(Graphics2D g2, int x, int y, int w, int h, Color fill, Color outline) {
        g2.setColor(fill);
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(outline);
        g2.drawRoundRect(x, y, w, h, 10, 10);
    }

    private void drawFingersGeneric(Graphics2D g2, int cx, int cy, Color skin, Color outline) {
        for (int i = 0; i < 4; i++) {
            drawFinger(g2, cx - 18 + (i * 11), cy - 50, 10, 36, (i - 1.5) * 8, skin, outline);
        }
        drawFinger(g2, cx - 36, cy - 10, 24, 10, -25, skin, outline);
    }

    private void drawMotionArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(UITheme.CYAN);
        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1, y1, x2, y2);
        g2.fillOval(x2 - 4, y2 - 4, 8, 8);
    }

    private void drawCircularArrow(Graphics2D g2, int cx, int cy) {
        g2.setColor(UITheme.WARNING);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(cx - 24, cy - 24, 48, 48, 30, 280);
        g2.fillPolygon(new int[]{cx + 24, cx + 18, cx + 30}, new int[]{cy - 4, cy - 14, cy - 14}, 3);
    }

    private void drawHeart(Graphics2D g2, int x, int y, int w, int h) {
        GeneralPath path = new GeneralPath();
        path.moveTo(x + w / 2.0, y + h / 4.0);
        path.curveTo(x + w / 2.0, y, x, y, x, y + h / 3.0);
        path.curveTo(x, y + (h * 2.0 / 3.0), x + w / 2.0, y + h, x + w / 2.0, y + h);
        path.curveTo(x + w / 2.0, y + h, x + w, y + (h * 2.0 / 3.0), x + w, y + h / 3.0);
        path.curveTo(x + w, y, x + w / 2.0, y, x + w / 2.0, y + h / 4.0);
        path.closePath();
        g2.fill(path);
    }
}
