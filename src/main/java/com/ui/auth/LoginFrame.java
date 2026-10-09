package com.ui.auth;

import com.model.TaiKhoan;
import com.service.AuthService;
import com.ui.main.MainFrame;
import com.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** A bright, welcoming sign-in screen for the PeopleOS workspace. */
public class LoginFrame extends JFrame {
    private static final String APP_TITLE = "PeopleOS · Quản lý Nhân sự & Tiền lương";
    private final AuthService authService = new AuthService();
    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap;
    private JButton btnThoat;
    private JLabel lblStatus;
    private boolean loginInProgress;

    public LoginFrame() {
        UITheme.setupGlobalUI();
        initComponents();
        setupLayout();
        setupEvents();
        setupFrame();
    }

    private void initComponents() {
        txtTenDangNhap = new JTextField(20);
        txtMatKhau = new JPasswordField(20);
        UITheme.styleTextField(txtTenDangNhap);
        UITheme.stylePasswordField(txtMatKhau);
        txtTenDangNhap.setPreferredSize(new Dimension(360, 46));
        txtTenDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        txtTenDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtMatKhau.setPreferredSize(new Dimension(360, 46));
        txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        txtMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtTenDangNhap.getAccessibleContext().setAccessibleName("Tên đăng nhập");
        txtMatKhau.getAccessibleContext().setAccessibleName("Mật khẩu");

        btnDangNhap = new UITheme.ModernButton("Đăng nhập",
                UITheme.PRIMARY, UITheme.PRIMARY_HOVER, Color.WHITE, UITheme.PRIMARY_HOVER);
        btnDangNhap.setIcon(UITheme.icon("arrow", 18, Color.WHITE));
        btnDangNhap.setHorizontalTextPosition(SwingConstants.LEFT);
        btnDangNhap.setIconTextGap(12);
        btnDangNhap.setPreferredSize(new Dimension(360, 46));
        btnThoat = new JButton("Thoát ứng dụng");
        UITheme.styleSecondaryButton(btnThoat);
        btnThoat.setPreferredSize(new Dimension(160, 38));
        lblStatus = text(" ", 12, Font.PLAIN, UITheme.DANGER_TEXT);
        lblStatus.setPreferredSize(new Dimension(360, 34));
        lblStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
    }

    private void setupLayout() {
        JPanel root = new JPanel(new BorderLayout(22, 0));
        root.setBackground(UITheme.BG_APP);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        SoftPanel story = new SoftPanel(new Color(237, 241, 255), null, 24);
        story.setPreferredSize(new Dimension(428, 0));
        story.setLayout(new BorderLayout(0, 14));
        story.setBorder(new EmptyBorder(28, 30, 26, 30));
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        JLabel logo = new JLabel(UITheme.icon("users", 25, UITheme.PRIMARY));
        logo.setBorder(new EmptyBorder(0, 0, 0, 11));
        brand.add(logo);
        brand.add(text("PeopleOS", 25, Font.BOLD, UITheme.TEXT_MAIN));
        story.add(brand, BorderLayout.NORTH);

        JPanel storyBody = column();
        storyBody.add(Box.createVerticalStrut(30));
        storyBody.add(text("CON NGƯỜI LÀ TRỌNG TÂM", 10, Font.BOLD, UITheme.PRIMARY));
        storyBody.add(Box.createVerticalStrut(13));
        storyBody.add(text("<html>Chăm chút đội ngũ.<br>Vững bước mỗi ngày.</html>", 30, Font.BOLD, UITheme.TEXT_MAIN));
        storyBody.add(Box.createVerticalStrut(13));
        storyBody.add(text("<html>Kết nối nhân sự, chấm công và tiền lương<br>trong một không gian làm việc thống nhất.</html>",
                13, Font.PLAIN, UITheme.TEXT_MUTED));
        storyBody.add(Box.createVerticalStrut(18));
        TeamIllustration art = new TeamIllustration();
        art.setAlignmentX(Component.LEFT_ALIGNMENT);
        art.setMaximumSize(new Dimension(Integer.MAX_VALUE, 216));
        storyBody.add(art);
        storyBody.add(Box.createVerticalStrut(14));
        storyBody.add(feature("Thông tin rõ ràng, quản lý dễ dàng"));
        storyBody.add(Box.createVerticalStrut(12));
        storyBody.add(feature("Mọi công việc, một nơi duy nhất"));
        storyBody.add(Box.createVerticalGlue());
        story.add(storyBody, BorderLayout.CENTER);
        story.add(text("NHÂN SỰ & TIỀN LƯƠNG", 10, Font.BOLD, UITheme.TEXT_MUTED), BorderLayout.SOUTH);
        root.add(story, BorderLayout.WEST);

        SoftPanel card = new SoftPanel(Color.WHITE, UITheme.BORDER, 24);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        JPanel form = column();
        form.setPreferredSize(new Dimension(360, Boolean.getBoolean("app.demo") ? 568 : 480));
        form.add(UITheme.createBadge("KHÔNG GIAN LÀM VIỆC", UITheme.PRIMARY,
                UITheme.PRIMARY_LIGHT, UITheme.PRIMARY_BORDER));
        form.add(Box.createVerticalStrut(18));
        form.add(text("Chào mừng trở lại", 27, Font.BOLD, UITheme.TEXT_MAIN));
        form.add(Box.createVerticalStrut(8));
        form.add(text("Đăng nhập để tiếp tục công việc của bạn.", 13, Font.PLAIN, UITheme.TEXT_MUTED));
        form.add(Box.createVerticalStrut(30));

        JLabel userLabel = text("Tên đăng nhập", 13, Font.BOLD, UITheme.TEXT_MAIN);
        userLabel.setLabelFor(txtTenDangNhap);
        form.add(userLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(txtTenDangNhap);
        form.add(Box.createVerticalStrut(18));
        JLabel passwordLabel = text("Mật khẩu", 13, Font.BOLD, UITheme.TEXT_MAIN);
        passwordLabel.setLabelFor(txtMatKhau);
        form.add(passwordLabel);
        form.add(Box.createVerticalStrut(8));
        form.add(txtMatKhau);
        form.add(Box.createVerticalStrut(10));

        JCheckBox showPassword = new JCheckBox("Hiện mật khẩu");
        showPassword.setOpaque(false);
        showPassword.setFont(UITheme.FONT_BODY);
        showPassword.setForeground(UITheme.TEXT_MUTED);
        showPassword.setBorder(new EmptyBorder(0, 0, 0, 0));
        showPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        char echo = txtMatKhau.getEchoChar();
        showPassword.addActionListener(e -> txtMatKhau.setEchoChar(showPassword.isSelected() ? (char) 0 : echo));
        form.add(showPassword);
        form.add(Box.createVerticalStrut(4));
        form.add(lblStatus);
        form.add(Box.createVerticalStrut(4));
        btnDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(btnDangNhap);
        form.add(Box.createVerticalStrut(16));
        form.add(text("Sử dụng tài khoản đã được cấp để đăng nhập.", 11, Font.PLAIN, UITheme.TEXT_MUTED));

        if (Boolean.getBoolean("app.demo")) {
            form.add(Box.createVerticalStrut(18));
            JPanel sample = new JPanel(new BorderLayout(0, 8));
            sample.setOpaque(false);
            sample.setAlignmentX(Component.LEFT_ALIGNMENT);
            sample.add(text("TÀI KHOẢN DÙNG THỬ", 10, Font.BOLD, UITheme.TEXT_MUTED), BorderLayout.NORTH);
            JPanel chips = new JPanel(new GridLayout(1, 4, 6, 0));
            chips.setOpaque(false);
            addQuickLoginChip(chips, "Admin", "admin", "");
            addQuickLoginChip(chips, "HR Manager", "hr_manager", "");
            addQuickLoginChip(chips, "Kế toán", "payroll_officer", "");
            addQuickLoginChip(chips, "Nhân viên", "employee01", "");
            sample.add(chips, BorderLayout.CENTER);
            sample.setMaximumSize(new Dimension(Integer.MAX_VALUE, 57));
            form.add(sample);
        }
        form.add(Box.createVerticalGlue());
        JPanel exit = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        exit.setOpaque(false);
        exit.setAlignmentX(Component.LEFT_ALIGNMENT);
        exit.add(btnThoat);
        form.add(Box.createVerticalStrut(18));
        form.add(exit);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        card.add(form, constraints);
        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void addQuickLoginChip(JPanel parent, String label, String user, String pass) {
        JButton button = new JButton(label);
        button.setUI(new UITheme.RoundedButtonUI(10, UITheme.PRIMARY_BORDER));
        button.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 10));
        button.setForeground(UITheme.PRIMARY);
        button.setBackground(UITheme.PRIMARY_LIGHT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(7, 3, 7, 3));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> {
            txtTenDangNhap.setText(user);
            txtMatKhau.setText(pass);
            lblStatus.setText("Đã nạp tài khoản: " + label);
            lblStatus.setForeground(UITheme.INFO_TEXT);
        });
        parent.add(button);
    }

    private static JPanel column() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private static JLabel text(String value, int size, int style, Color color) {
        JLabel label = new JLabel(value);
        label.setFont(new Font(UITheme.FONT_FAMILY, style, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private static JPanel feature(String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.add(new JLabel(UITheme.icon("check", 17, UITheme.PRIMARY)), BorderLayout.WEST);
        row.add(text(value, 12, Font.PLAIN, UITheme.TEXT_MAIN), BorderLayout.CENTER);
        return row;
    }

    private void setupEvents() {
        btnDangNhap.addActionListener(this::handleLogin);
        btnThoat.addActionListener(e -> System.exit(0));

        txtMatKhau.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(null);
                }
            }
        });

        txtTenDangNhap.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtMatKhau.requestFocusInWindow();
                }
            }
        });
    }

    private void handleLogin(ActionEvent e) {
        if (loginInProgress) return;
        String tenDangNhap = txtTenDangNhap.getText().trim();
        char[] entered = txtMatKhau.getPassword();
        String matKhau = new String(entered);
        java.util.Arrays.fill(entered, '\0');

        if (tenDangNhap.isEmpty() || matKhau.isEmpty()) {
            lblStatus.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            lblStatus.setForeground(UITheme.DANGER_TEXT);
            return;
        }

        loginInProgress = true;
        btnDangNhap.setEnabled(false);
        txtTenDangNhap.setEnabled(false);
        txtMatKhau.setEnabled(false);
        lblStatus.setText("Đang xác thực thông tin...");
        lblStatus.setForeground(UITheme.INFO_TEXT);

        SwingWorker<TaiKhoan, Void> worker = new SwingWorker<TaiKhoan, Void>() {
            @Override
            protected TaiKhoan doInBackground() throws Exception {
                return authService.login(tenDangNhap, matKhau);
            }

            @Override
            protected void done() {
                try {
                    TaiKhoan taiKhoan = get();
                    if (taiKhoan == null) {
                        finishLoginAttempt();
                        lblStatus.setText("Tên đăng nhập hoặc mật khẩu không chính xác!");
                        lblStatus.setForeground(UITheme.DANGER_TEXT);
                        txtMatKhau.setText("");
                        txtMatKhau.requestFocusInWindow();
                        return;
                    }

                    lblStatus.setText("Đăng nhập thành công!");
                    lblStatus.setForeground(UITheme.SUCCESS_TEXT);
                    txtMatKhau.setText("");
                    openMainFrame();
                } catch (Exception ex) {
                    finishLoginAttempt();
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    String msg = cause.getMessage();
                    if (msg == null || msg.trim().isEmpty()) {
                        msg = "Đăng nhập thất bại!";
                    }
                    lblStatus.setText(msg);
                    lblStatus.setForeground(UITheme.DANGER_TEXT);
                    txtMatKhau.setText("");
                    txtMatKhau.requestFocusInWindow();
                }
            }
        };
        worker.execute();
    }

    private void finishLoginAttempt() {
        loginInProgress = false;
        btnDangNhap.setEnabled(true);
        txtTenDangNhap.setEnabled(true);
        txtMatKhau.setEnabled(true);
    }

    private void openMainFrame() {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
        this.dispose();
    }

    private void setupFrame() {
        setTitle(APP_TITLE + " · Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 720);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setResizable(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                txtTenDangNhap.requestFocusInWindow();
            }
        });
    }

    private static class SoftPanel extends JPanel {
        private final Color outline;
        private final int radius;
        SoftPanel(Color background, Color outline, int radius) {
            this.outline = outline;
            this.radius = radius;
            setOpaque(false);
            setBackground(background);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (outline != null) {
                g2.setColor(outline);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Decorative calendar and team, deliberately free of sample business data. */
    private static class TeamIllustration extends JPanel {
        TeamIllustration() {
            setOpaque(false);
            setPreferredSize(new Dimension(360, 216));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(Math.max(0, (getWidth() - 330) / 2), 5);
            g2.setColor(new Color(220, 227, 255));
            g2.fillOval(40, 0, 230, 206);
            g2.setColor(new Color(207, 217, 250));
            g2.fillRoundRect(76, 33, 224, 143, 18, 18);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(62, 19, 224, 143, 18, 18);
            g2.setColor(UITheme.PRIMARY);
            g2.fillRoundRect(62, 19, 224, 37, 18, 18);
            g2.fillRect(62, 39, 224, 17);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 11));
            g2.drawString("LỊCH LÀM VIỆC", 80, 43);
            UITheme.icon("calendar", 17, Color.WHITE).paintIcon(this, g2, 249, 29);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 7; col++) {
                    g2.setColor(row == 1 && col == 3 ? UITheme.PRIMARY : new Color(237, 241, 250));
                    g2.fillRoundRect(80 + col * 27, 74 + row * 25, 17, 15, 5, 5);
                }
            }
            g2.setColor(new Color(191, 204, 245));
            g2.fillRoundRect(19, 130, 150, 64, 15, 15);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(11, 122, 150, 64, 15, 15);
            Color[] colors = { UITheme.PRIMARY, new Color(13, 148, 136), new Color(217, 119, 6) };
            for (int i = 0; i < colors.length; i++) {
                int x = 27 + i * 41;
                g2.setColor(new Color(240, 243, 255));
                g2.fillOval(x, 134, 32, 32);
                UITheme.icon("users", 20, colors[i]).paintIcon(this, g2, x + 6, 140);
            }
            g2.setColor(new Color(209, 250, 229));
            g2.fillOval(264, 142, 43, 43);
            UITheme.icon("check", 23, UITheme.SUCCESS_TEXT).paintIcon(this, g2, 274, 152);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        UITheme.setupGlobalUI();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
