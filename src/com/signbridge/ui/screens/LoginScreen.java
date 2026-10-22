package com.signbridge.ui.screens;

import com.signbridge.db.DBConnection;
import com.signbridge.service.AuthService;
import com.signbridge.ui.MainFrame;
import com.signbridge.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LoginScreen extends JPanel {
    private final MainFrame mainFrame;
    private final JTextField txtUser;
    private final JPasswordField txtPass;
    private final JTextField txtRegUser;
    private final JPasswordField txtRegPass;
    private final JTextField txtRegName;
    private final JLabel lblStatus;
    private final CardLayout formLayout;
    private final JPanel formContainer;

    public LoginScreen(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_DARK);

        JPanel centerCard = new JPanel(new BorderLayout(16, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        centerCard.setOpaque(false);
        centerCard.setPreferredSize(new Dimension(440, 520));
        centerCard.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel header = new JPanel(new GridLayout(2, 1, 4, 4));
        header.setOpaque(false);

        JLabel title = new JLabel("SIGNBRIDGE", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UITheme.PRIMARY);

        JLabel subtitle = new JLabel("Sign Language Learning & Gaming Platform", SwingConstants.CENTER);
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        header.add(title);
        header.add(subtitle);
        centerCard.add(header, BorderLayout.NORTH);

        formLayout = new CardLayout();
        formContainer = new JPanel(formLayout);
        formContainer.setOpaque(false);

        // LOGIN
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setOpaque(false);

        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(createFieldLabel("Username:"));
        txtUser = createTextField();
        txtUser.setText("aayushi");
        loginPanel.add(txtUser);

        loginPanel.add(Box.createVerticalStrut(12));
        loginPanel.add(createFieldLabel("Password:"));
        txtPass = createPasswordField();
        txtPass.setText("pass123");
        loginPanel.add(txtPass);

        loginPanel.add(Box.createVerticalStrut(18));
        JButton btnLogin = UITheme.createPrimaryButton("Sign In");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.addActionListener(e -> handleLogin());
        loginPanel.add(btnLogin);

        loginPanel.add(Box.createVerticalStrut(12));
        JButton btnGoRegister = new JButton("Don't have an account? Register here");
        btnGoRegister.setFont(UITheme.FONT_SMALL);
        btnGoRegister.setForeground(UITheme.CYAN);
        btnGoRegister.setContentAreaFilled(false);
        btnGoRegister.setBorderPainted(false);
        btnGoRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGoRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGoRegister.addActionListener(e -> formLayout.show(formContainer, "REGISTER"));
        loginPanel.add(btnGoRegister);

        JLabel lblDemo = new JLabel("Default Profile: aayushi / pass123", SwingConstants.CENTER);
        lblDemo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDemo.setForeground(UITheme.WARNING);
        lblDemo.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(Box.createVerticalStrut(12));
        loginPanel.add(lblDemo);

        // REGISTER
        JPanel regPanel = new JPanel();
        regPanel.setLayout(new BoxLayout(regPanel, BoxLayout.Y_AXIS));
        regPanel.setOpaque(false);

        regPanel.add(createFieldLabel("Full Name:"));
        txtRegName = createTextField();
        regPanel.add(txtRegName);

        regPanel.add(Box.createVerticalStrut(8));
        regPanel.add(createFieldLabel("Choose Username:"));
        txtRegUser = createTextField();
        regPanel.add(txtRegUser);

        regPanel.add(Box.createVerticalStrut(8));
        regPanel.add(createFieldLabel("Create Password:"));
        txtRegPass = createPasswordField();
        regPanel.add(txtRegPass);

        regPanel.add(Box.createVerticalStrut(16));
        JButton btnRegister = UITheme.createPrimaryButton("Create Account");
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnRegister.addActionListener(e -> handleRegister());
        regPanel.add(btnRegister);

        regPanel.add(Box.createVerticalStrut(10));
        JButton btnGoLogin = new JButton("Already registered? Back to Sign In");
        btnGoLogin.setFont(UITheme.FONT_SMALL);
        btnGoLogin.setForeground(UITheme.CYAN);
        btnGoLogin.setContentAreaFilled(false);
        btnGoLogin.setBorderPainted(false);
        btnGoLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGoLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGoLogin.addActionListener(e -> formLayout.show(formContainer, "LOGIN"));
        regPanel.add(btnGoLogin);

        formContainer.add(loginPanel, "LOGIN");
        formContainer.add(regPanel, "REGISTER");
        centerCard.add(formContainer, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(4, 4));
        footer.setOpaque(false);
        String dbMsg = DBConnection.isConnected() ? "[DATABASE CONNECTED: SQLite]" : "[DATABASE DISCONNECTED]";
        lblStatus = new JLabel(dbMsg, SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(DBConnection.isConnected() ? UITheme.SUCCESS : UITheme.DANGER);
        footer.add(lblStatus, BorderLayout.CENTER);

        centerCard.add(footer, BorderLayout.SOUTH);
        add(centerCard);
    }

    private void handleLogin() {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = AuthService.getInstance().login(u, p);
        if (success) {
            mainFrame.onLoginSuccess();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Try username 'aayushi' and password 'pass123'.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String name = txtRegName.getText().trim();
        String user = txtRegUser.getText().trim();
        String pass = new String(txtRegPass.getPassword());

        if (name.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please complete all fields.", "Registration", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = AuthService.getInstance().register(user, pass, name);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Account created successfully! Welcome to SignBridge, " + name + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.onLoginSuccess();
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists. Please choose another.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_LIGHT);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_BODY);
        tf.setBackground(UITheme.SURFACE);
        tf.setForeground(UITheme.TEXT_LIGHT);
        tf.setCaretColor(Color.WHITE);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true), new EmptyBorder(6, 12, 6, 12)));
        return tf;
    }

    private JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(UITheme.FONT_BODY);
        pf.setBackground(UITheme.SURFACE);
        pf.setForeground(UITheme.TEXT_LIGHT);
        pf.setCaretColor(Color.WHITE);
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pf.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true), new EmptyBorder(6, 12, 6, 12)));
        return pf;
    }
}
