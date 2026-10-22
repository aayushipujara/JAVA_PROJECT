package com.signbridge.ui.components;

import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import java.awt.*;

public class TimerBar extends JProgressBar {
    public TimerBar(int maxSeconds) {
        super(0, maxSeconds * 10);
        setValue(maxSeconds * 10);
        setPreferredSize(new Dimension(200, 10));
        setStringPainted(false);
        setBorderPainted(false);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background track
        g2.setColor(UITheme.SURFACE);
        g2.fillRoundRect(0, 0, w, h, h, h);

        // Fill progress
        double percent = (double) getValue() / getMaximum();
        int fillW = (int) (w * percent);

        Color fillColor;
        if (percent > 0.5) {
            fillColor = UITheme.SUCCESS;
        } else if (percent > 0.25) {
            fillColor = UITheme.WARNING;
        } else {
            fillColor = UITheme.DANGER;
        }

        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, fillW, h, h, h);
        g2.dispose();
    }
}
